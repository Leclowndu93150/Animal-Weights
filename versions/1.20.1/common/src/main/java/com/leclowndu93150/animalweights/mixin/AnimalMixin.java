package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.DisplayTracker;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.WeightHolder;
import com.leclowndu93150.animalweights.WeightSyncedData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.display.DisplayOverlay;
import com.leclowndu93150.animalweights.display.LootSampler;
import com.leclowndu93150.animalweights.goal.WanderToHabitatGoal;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import com.leclowndu93150.animalweights.habitat.WeightTickLogic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob implements DisplayTracker, WeightHolder {
    @Unique
    private static final String ANIMALWEIGHTS_TAG = "AnimalWeightsData";

    @Unique
    private boolean animalweights$goalRegistered;

    @Unique
    private final List<Entity> animalweights$displays = new ArrayList<>();

    @Unique
    private int animalweights$lastDisplayedWeight = Integer.MIN_VALUE;

    @Unique
    private boolean animalweights$previewSampled;

    @Unique
    private WeightData animalweights$weightData;

    protected AnimalMixin(EntityType<? extends AgeableMob> type, Level level) {
        super(type, level);
    }

    @Override
    public WeightData animalweights$getWeightData() {
        if (this.level().isClientSide && this.animalweights$weightData == null) {
            int synced = this.entityData.get(WeightSyncedData.WEIGHT);
            this.animalweights$weightData = new WeightData(synced, "");
        }
        return this.animalweights$weightData;
    }

    @Override
    public void animalweights$setWeightData(WeightData data) {
        this.animalweights$weightData = data;
        if (!this.level().isClientSide && data != null) {
            this.entityData.set(WeightSyncedData.WEIGHT, data.getWeight());
        }
    }

    @Override
    public List<Entity> animalweights$displays() {
        return Collections.unmodifiableList(this.animalweights$displays);
    }

    @Override
    public void animalweights$trackDisplay(Entity display) {
        this.animalweights$displays.add(display);
    }

    @Override
    public void animalweights$clearDisplays() {
        this.animalweights$displays.clear();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void animalweights$saveWeight(CompoundTag tag, CallbackInfo ci) {
        if (this.animalweights$weightData != null) {
            CompoundTag sub = new CompoundTag();
            this.animalweights$weightData.save(sub);
            tag.put(ANIMALWEIGHTS_TAG, sub);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void animalweights$loadWeight(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(ANIMALWEIGHTS_TAG)) {
            this.animalweights$weightData = WeightData.load(tag.getCompound(ANIMALWEIGHTS_TAG));
        }
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void animalweights$serverTick(CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        if (!this.animalweights$goalRegistered) {
            this.animalweights$goalRegistered = true;
            this.goalSelector.addGoal(5, new WanderToHabitatGoal((PathfinderMob) (Object) this, 1.0));
        }
        WeightTickLogic.tick(self, level);
        int currentWeight = WeightAttachment.getWeight(self);
        if (!this.animalweights$previewSampled && currentWeight > 1) {
            this.animalweights$previewSampled = true;
            List<Item> sampled = LootSampler.sample(self, level);
            if (!sampled.isEmpty()) {
                WeightAttachment.setLootPreview(self, LootSampler.encode(sampled));
            }
        }
        AnimalWeightsConfig cfg = ConfigManager.get();
        if (currentWeight != this.animalweights$lastDisplayedWeight) {
            this.animalweights$lastDisplayedWeight = currentWeight;
            if (cfg.enableOverlay) {
                DisplayOverlay.refresh(self, level);
            }
        }
        if (cfg.enableSickParticles && currentWeight <= cfg.minWeight && level.getGameTime() % 30 == 0) {
            level.sendParticles(ParticleTypes.MYCELIUM,
                self.getX(), self.getY() + self.getBbHeight() * 0.7, self.getZ(),
                3, 0.25, 0.2, 0.25, 0.0);
        }
    }

    @Inject(method = "canFallInLove", at = @At("HEAD"), cancellable = true)
    private void animalweights$blockSickBreeding(CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (WeightAttachment.getWeight(self) <= 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void animalweights$scaleXp(CallbackInfoReturnable<Integer> cir) {
        Animal self = (Animal) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        int base = cir.getReturnValueI();
        int weight = Math.max(1, WeightAttachment.getWeight(self));
        int scaled = ConfigManager.get().xpScalingMode.apply(base, weight);
        if (HabitatScanner.isNearWaterOrVillageCached(self, level)) {
            scaled += 1;
        }
        cir.setReturnValue(scaled);
    }
}

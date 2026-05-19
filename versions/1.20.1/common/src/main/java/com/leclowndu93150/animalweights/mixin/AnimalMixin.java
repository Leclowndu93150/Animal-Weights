package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.WeightHolder;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.display.LegacyDisplayCleanup;
import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.goal.WanderToHabitatGoal;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import com.leclowndu93150.animalweights.habitat.WeightTickLogic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob implements WeightHolder {
    @Unique
    private static final String ANIMALWEIGHTS_TAG = "AnimalWeightsData";

    @Unique
    private boolean animalweights$goalRegistered;

    @Unique
    private boolean animalweights$legacyDisplaysCleaned;

    @Unique
    private WeightData animalweights$weightData;

    protected AnimalMixin(EntityType<? extends AgeableMob> type, Level level) {
        super(type, level);
    }

    @Override
    public WeightData animalweights$getWeightData() {
        return this.animalweights$weightData;
    }

    @Override
    public void animalweights$setWeightData(WeightData data) {
        this.animalweights$weightData = data;
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
        if (!this.animalweights$legacyDisplaysCleaned) {
            this.animalweights$legacyDisplaysCleaned = true;
            LegacyDisplayCleanup.cleanup(self);
        }
        if (!this.animalweights$goalRegistered) {
            this.animalweights$goalRegistered = true;
            this.goalSelector.addGoal(5, new WanderToHabitatGoal((PathfinderMob) (Object) this, 1.0));
        }
        WeightTickLogic.tick(self, level);
        LootCache.ensureSampled(self, level);
        int currentWeight = WeightAttachment.getWeight(self);
        if (ConfigManager.get().enableSickParticles && currentWeight <= ConfigManager.get().minWeight && level.getGameTime() % 30 == 0) {
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

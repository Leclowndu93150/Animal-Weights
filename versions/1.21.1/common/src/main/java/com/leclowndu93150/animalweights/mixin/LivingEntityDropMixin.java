package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {
    @Shadow
    protected abstract void dropFromLootTable(DamageSource source, boolean playerKilled);

    @Unique
    private boolean animalweights$reentrant;

    @Inject(
        method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
        at = @At("HEAD")
    )
    private void animalweights$scaleDrops(DamageSource source, boolean playerKilled, CallbackInfo ci) {
        if (this.animalweights$reentrant) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Animal animal)) {
            return;
        }
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        int extra = ConfigManager.get().dropScalingMode.extraRolls(WeightAttachment.getWeight(animal));
        if (HabitatScanner.isNearWaterOrVillageCached(animal, level)) {
            extra += 1;
        }
        if (extra <= 0) {
            return;
        }
        this.animalweights$reentrant = true;
        try {
            for (int i = 0; i < extra; i++) {
                this.dropFromLootTable(source, playerKilled);
            }
        } finally {
            this.animalweights$reentrant = false;
        }
    }
}

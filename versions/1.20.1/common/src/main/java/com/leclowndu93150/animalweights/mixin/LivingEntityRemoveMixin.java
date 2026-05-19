package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.display.LegacyDisplayCleanup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRemoveMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void animalweights$cleanupLegacyDisplays(Entity.RemovalReason reason, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Animal) {
            LegacyDisplayCleanup.cleanup(self);
        }
    }
}

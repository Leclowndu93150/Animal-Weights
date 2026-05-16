package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.DisplayTracker;
import com.leclowndu93150.animalweights.display.DisplayOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDieMixin {
    @Inject(method = "die", at = @At("TAIL"))
    private void animalweights$cleanupDisplays(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof DisplayTracker tracker) {
            DisplayOverlay.onDeath(self, tracker);
        }
    }
}

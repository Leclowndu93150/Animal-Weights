package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.DisplayTracker;
import com.leclowndu93150.animalweights.display.DisplayOverlay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class AnimalRemoveMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void animalweights$cleanupOverlay(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Animal && self instanceof DisplayTracker tracker) {
            DisplayOverlay.cleanup(self, tracker);
        }
    }
}

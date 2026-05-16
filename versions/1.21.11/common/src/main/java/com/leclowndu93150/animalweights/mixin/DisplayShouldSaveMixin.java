package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.display.DisplayOverlay;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class DisplayShouldSaveMixin {
    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true)
    private void animalweights$skipSaveForOverlay(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Display && self.getTags().contains(DisplayOverlay.OVERLAY_TAG)) {
            cir.setReturnValue(false);
        }
    }
}

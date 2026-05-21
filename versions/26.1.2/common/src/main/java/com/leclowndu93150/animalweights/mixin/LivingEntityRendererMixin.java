package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.SickState;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void animalweights$captureSick(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        boolean sick = cfg.enableSickTint && entity instanceof Animal animal && !AnimalWeightsRules.isDisabled(animal) && WeightAttachment.getWeight(animal) <= cfg.sickThreshold;
        ((SickState) state).animalweights$setSick(sick);
    }

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void animalweights$applySickTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (((SickState) state).animalweights$isSick()) {
            cir.setReturnValue(ConfigManager.get().sickTintColor);
        }
    }
}

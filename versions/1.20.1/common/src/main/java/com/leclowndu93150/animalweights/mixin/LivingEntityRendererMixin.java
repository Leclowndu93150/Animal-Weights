package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    private static final ThreadLocal<LivingEntity> animalweights$currentEntity = new ThreadLocal<>();

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void animalweights$captureEntity(T entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo ci) {
        animalweights$currentEntity.set(entity);
    }

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("RETURN")
    )
    private void animalweights$releaseEntity(T entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo ci) {
        animalweights$currentEntity.remove();
    }

    @Redirect(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")
    )
    private void animalweights$tintSick(EntityModel<T> model, PoseStack pose, VertexConsumer consumer, int packedLight, int overlay, float r, float g, float b, float a) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        LivingEntity entity = animalweights$currentEntity.get();
        if (cfg.enableSickTint && entity instanceof Animal animal && WeightAttachment.getWeight(animal) <= cfg.minWeight) {
            int color = cfg.sickTintColor;
            a = ((color >> 24) & 0xFF) / 255.0F;
            r = ((color >> 16) & 0xFF) / 255.0F;
            g = ((color >> 8) & 0xFF) / 255.0F;
            b = (color & 0xFF) / 255.0F;
        }
        model.renderToBuffer(pose, consumer, packedLight, overlay, r, g, b, a);
    }
}

package com.leclowndu93150.animalweights.display;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public final class WeightOverlayRenderer {
    private static final float ROW_HEIGHT = 0.35F;
    private static final float ICON_SCALE = 0.35F;
    private static final float TEXT_SCALE = 0.025F;
    private static final float ICON_WIDTH = ICON_SCALE;
    private static final float PAIR_GAP = 0.05F;
    private static final float MIN_VISIBLE_ALPHA = 4.0F / 255.0F;
    private static final int WHITE = 0xFFFFFF;
    private static final int SICK_GREEN = 0x55FF55;

    private WeightOverlayRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        AnimalWeightsConfig cfg = ConfigManager.get();
        if (!cfg.enableOverlay) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        Vec3 camPos = camera.position();
        Font font = mc.font;
        ItemModelResolver resolver = mc.getItemModelResolver();
        Quaternionf billboard = camera.rotation();

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Animal animal) || animal.isBaby() || animal.isRemoved()) {
                continue;
            }
            int weight = WeightAttachment.getWeight(animal);
            boolean sick = weight <= 0;
            boolean bonus = weight > 1;
            if (!sick && !bonus) {
                OverlayFadeTracker.clear(animal);
                continue;
            }
            float alpha = OverlayFadeTracker.alpha(animal, OverlayVisibility.shouldRender(animal, player), weight);
            if (alpha < MIN_VISIBLE_ALPHA) continue;

            double lerpX = animal.xOld + (animal.getX() - animal.xOld) * partialTicks;
            double lerpY = animal.yOld + (animal.getY() - animal.yOld) * partialTicks;
            double lerpZ = animal.zOld + (animal.getZ() - animal.zOld) * partialTicks;
            double dx = lerpX - camPos.x;
            double dy = lerpY - camPos.y + animal.getBbHeight() + 0.5;
            double dz = lerpZ - camPos.z;

            BlockPos lightPos = BlockPos.containing(lerpX, lerpY + animal.getBbHeight(), lerpZ);
            int light = LightCoordsUtil.pack(
                mc.level.getBrightness(LightLayer.BLOCK, lightPos),
                mc.level.getBrightness(LightLayer.SKY, lightPos)
            );

            if (sick) {
                drawText(poseStack, collector, font, billboard,
                    "Sick", dx, dy, dz, 0.0F, 0.0F, light, colorWithAlpha(SICK_GREEN, alpha));
                continue;
            }

            List<ItemStack> icons = resolveIcons(animal);
            if (icons.isEmpty()) continue;

            String label = "+" + (weight - 1);
            float textWidth = font.width(label) * TEXT_SCALE;
            float totalWidth = ICON_WIDTH + PAIR_GAP + textWidth;
            float iconX = -totalWidth / 2.0F + ICON_WIDTH / 2.0F;
            float textX = totalWidth / 2.0F - textWidth / 2.0F;
            for (int i = 0; i < icons.size(); i++) {
                float yOffset = ROW_HEIGHT * (icons.size() - 1 - i);
                drawItem(poseStack, collector, resolver, billboard, icons.get(i),
                    dx, dy, dz, iconX, yOffset, light, mc, alpha);
                drawText(poseStack, collector, font, billboard, label,
                    dx, dy, dz, textX, yOffset, light, colorWithAlpha(WHITE, alpha));
            }
        }
    }

    private static List<ItemStack> resolveIcons(Animal animal) {
        List<Identifier> ids = LootCache.get(animal.getType());
        if (ids.isEmpty()) return List.of();
        List<ItemStack> out = new ArrayList<>(ids.size());
        for (Identifier id : ids) {
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item != null) out.add(new ItemStack(item));
        }
        return out;
    }

    private static void drawItem(PoseStack pose, SubmitNodeCollector collector, ItemModelResolver resolver,
                                  Quaternionf billboard, ItemStack stack,
                                  double dx, double dy, double dz, float xOff, float yOff,
                                  int light, Minecraft mc, float alpha) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, mc.level, null, 0);
        pose.pushPose();
        pose.translate(dx, dy + yOff, dz);
        pose.mulPose(billboard);
        pose.translate(xOff, 0.0F, 0.0F);
        float iconScale = ICON_SCALE * alpha;
        pose.scale(iconScale, iconScale, iconScale);
        renderState.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    private static void drawText(PoseStack pose, SubmitNodeCollector collector, Font font,
                                  Quaternionf billboard, String text,
                                  double dx, double dy, double dz, float xOff, float yOff,
                                  int light, int color) {
        pose.pushPose();
        pose.translate(dx, dy + yOff, dz);
        pose.mulPose(billboard);
        pose.translate(xOff, 0.0F, 0.0F);
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        float width = font.width(text);
        float yShift = -font.lineHeight / 2.0F;
        collector.submitText(pose, -width / 2.0F, yShift, Component.literal(text).getVisualOrderText(), false,
            Font.DisplayMode.NORMAL, light, color, 0, 0);
        pose.popPose();
    }

    private static int colorWithAlpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F + 0.5F)));
        return (color & 0x00FFFFFF) | (a << 24);
    }
}

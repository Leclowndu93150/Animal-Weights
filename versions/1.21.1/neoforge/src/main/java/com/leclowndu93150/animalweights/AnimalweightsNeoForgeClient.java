package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.display.WeightOverlayRenderer;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Map;

public final class AnimalweightsNeoForgeClient {
    private AnimalweightsNeoForgeClient() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForgeClient::onRenderStage);
    }

    public static void applyWeight(int entityId, int weight) {
        WeightSyncClient.apply(entityId, weight);
    }

    public static void applyLootEntry(ResourceLocation type, List<ResourceLocation> items) {
        LootCache.putClient(type, items);
    }

    public static void applyLootSnapshot(Map<ResourceLocation, List<ResourceLocation>> entries) {
        LootCache.clear();
        entries.forEach(LootCache::putClient);
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        WeightOverlayRenderer.render(pose, buffers, event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        buffers.endBatch();
    }
}

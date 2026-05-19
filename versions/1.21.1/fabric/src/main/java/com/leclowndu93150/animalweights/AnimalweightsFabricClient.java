package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.display.WeightOverlayRenderer;
import com.leclowndu93150.animalweights.network.LootEntryPayload;
import com.leclowndu93150.animalweights.network.LootSnapshotPayload;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class AnimalweightsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WeightSyncPayload.TYPE, (payload, ctx) ->
            WeightSyncClient.apply(payload.entityId, payload.weight));
        ClientPlayNetworking.registerGlobalReceiver(LootEntryPayload.TYPE, (payload, ctx) ->
            ctx.client().execute(() -> LootCache.putClient(payload.entityType, payload.items)));
        ClientPlayNetworking.registerGlobalReceiver(LootSnapshotPayload.TYPE, (payload, ctx) ->
            ctx.client().execute(() -> {
                LootCache.clear();
                payload.entries.forEach(LootCache::putClient);
            }));

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
            WeightOverlayRenderer.render(context.matrixStack(), buffers, context.camera(), context.tickCounter().getGameTimeDeltaPartialTick(false));
            buffers.endBatch();
        });
    }
}

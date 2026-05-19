package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.display.WeightOverlayRenderer;
import com.leclowndu93150.animalweights.network.LootSyncPayload;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalweightsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WeightSyncPayload.CHANNEL, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            int weight = buf.readVarInt();
            client.execute(() -> WeightSyncClient.apply(entityId, weight));
        });
        ClientPlayNetworking.registerGlobalReceiver(LootSyncPayload.ENTRY_CHANNEL, (client, handler, buf, responseSender) -> {
            ResourceLocation type = buf.readResourceLocation();
            int count = buf.readVarInt();
            List<ResourceLocation> items = new ArrayList<>(count);
            for (int i = 0; i < count; i++) items.add(buf.readResourceLocation());
            client.execute(() -> LootCache.putClient(type, items));
        });
        ClientPlayNetworking.registerGlobalReceiver(LootSyncPayload.SNAPSHOT_CHANNEL, (client, handler, buf, responseSender) -> {
            int count = buf.readVarInt();
            Map<ResourceLocation, List<ResourceLocation>> entries = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                ResourceLocation type = buf.readResourceLocation();
                int n = buf.readVarInt();
                List<ResourceLocation> items = new ArrayList<>(n);
                for (int j = 0; j < n; j++) items.add(buf.readResourceLocation());
                entries.put(type, items);
            }
            client.execute(() -> {
                LootCache.clear();
                entries.forEach(LootCache::putClient);
            });
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
            WeightOverlayRenderer.render(context.matrixStack(), buffers, context.camera(), context.tickDelta());
            buffers.endBatch();
        });
    }
}

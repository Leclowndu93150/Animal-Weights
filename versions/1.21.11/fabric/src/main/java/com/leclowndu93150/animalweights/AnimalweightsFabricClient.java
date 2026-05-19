package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootEntryPayload;
import com.leclowndu93150.animalweights.network.LootSnapshotPayload;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

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
    }
}

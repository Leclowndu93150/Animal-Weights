package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootEntryPayload;
import com.leclowndu93150.animalweights.network.LootSnapshotPayload;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "animalweights", value = Dist.CLIENT)
public final class AnimalweightsNeoForgeClient {
    private AnimalweightsNeoForgeClient() {
    }

    public static void init() {
    }

    public static void applyWeight(int entityId, int weight) {
        WeightSyncClient.apply(entityId, weight);
    }

    public static void applyLootEntry(Identifier type, List<Identifier> items) {
        LootCache.putClient(type, items);
    }

    public static void applyLootSnapshot(Map<Identifier, List<Identifier>> entries) {
        LootCache.clear();
        entries.forEach(LootCache::putClient);
    }

    @SubscribeEvent
    private static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(WeightSyncPayload.TYPE, (payload, ctx) -> applyWeight(payload.entityId, payload.weight));
        event.register(LootEntryPayload.TYPE, (payload, ctx) -> applyLootEntry(payload.entityType, payload.items));
        event.register(LootSnapshotPayload.TYPE, (payload, ctx) -> applyLootSnapshot(payload.entries));
    }
}

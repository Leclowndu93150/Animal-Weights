package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.WeightSyncClient;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

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
}

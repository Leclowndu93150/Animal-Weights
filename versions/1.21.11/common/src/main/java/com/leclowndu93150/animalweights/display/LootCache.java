package com.leclowndu93150.animalweights.display;

import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootCache {
    private static final Map<Identifier, List<Identifier>> ENTRIES = new HashMap<>();

    private LootCache() {
    }

    public static void ensureSampled(Animal animal, ServerLevel level) {
        Identifier typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (typeKey == null || ENTRIES.containsKey(typeKey)) {
            return;
        }
        List<Item> sampled = LootSampler.sample(animal, level);
        List<Identifier> ids = sampled.stream()
            .map(BuiltInRegistries.ITEM::getKey)
            .toList();
        ENTRIES.put(typeKey, ids);
        LootSyncDispatcher.broadcastEntry(typeKey, ids);
    }

    public static void putClient(Identifier typeKey, List<Identifier> items) {
        ENTRIES.put(typeKey, items);
    }

    public static List<Identifier> get(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) return List.of();
        List<Identifier> entry = ENTRIES.get(key);
        return entry == null ? List.of() : entry;
    }

    public static Map<Identifier, List<Identifier>> snapshot() {
        return ENTRIES;
    }

    public static void clear() {
        ENTRIES.clear();
    }
}

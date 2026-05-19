package com.leclowndu93150.animalweights.display;

import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootCache {
    private static final Map<ResourceLocation, List<ResourceLocation>> ENTRIES = new HashMap<>();

    private LootCache() {
    }

    public static void ensureSampled(Animal animal, ServerLevel level) {
        ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (typeKey == null || ENTRIES.containsKey(typeKey)) {
            return;
        }
        List<Item> sampled = LootSampler.sample(animal, level);
        List<ResourceLocation> ids = sampled.stream()
            .map(BuiltInRegistries.ITEM::getKey)
            .toList();
        ENTRIES.put(typeKey, ids);
        LootSyncDispatcher.broadcastEntry(typeKey, ids);
    }

    public static void putClient(ResourceLocation typeKey, List<ResourceLocation> items) {
        ENTRIES.put(typeKey, items);
    }

    public static List<ResourceLocation> get(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key == null) return List.of();
        List<ResourceLocation> entry = ENTRIES.get(key);
        return entry == null ? List.of() : entry;
    }

    public static Map<ResourceLocation, List<ResourceLocation>> snapshot() {
        return ENTRIES;
    }

    public static void clear() {
        ENTRIES.clear();
    }
}

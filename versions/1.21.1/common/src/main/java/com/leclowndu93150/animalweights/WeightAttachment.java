package com.leclowndu93150.animalweights;

import net.minecraft.world.entity.animal.Animal;

public final class WeightAttachment {
    private static Bridge bridge;

    private WeightAttachment() {
    }

    public static void install(Bridge impl) {
        bridge = impl;
    }

    public static WeightData get(Animal animal) {
        return bridge.get(animal);
    }

    public static int getWeight(Animal animal) {
        return get(animal).getWeight();
    }

    public static void setWeight(Animal animal, int weight) {
        WeightData current = get(animal);
        WeightData replacement = new WeightData(weight, current.getLootPreview());
        replacement.storeBonusCache(current.getBonusCacheTick(), current.getBonusCacheValue());
        bridge.set(animal, replacement);
    }

    public static String getLootPreview(Animal animal) {
        return get(animal).getLootPreview();
    }

    public static void setLootPreview(Animal animal, String csv) {
        WeightData current = get(animal);
        WeightData replacement = new WeightData(current.getWeight(), csv);
        replacement.storeBonusCache(current.getBonusCacheTick(), current.getBonusCacheValue());
        bridge.set(animal, replacement);
    }

    public interface Bridge {
        WeightData get(Animal animal);
        void set(Animal animal, WeightData data);
    }
}

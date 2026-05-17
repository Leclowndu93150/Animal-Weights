package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

public final class WeightAttachment {
    private WeightAttachment() {
    }

    public static WeightData get(Animal animal) {
        WeightHolder holder = (WeightHolder) animal;
        WeightData data = holder.animalweights$getWeightData();
        if (data == null) {
            data = new WeightData();
            holder.animalweights$setWeightData(data);
        }
        return data;
    }

    public static int getWeight(Animal animal) {
        return get(animal).getWeight();
    }

    public static void setWeight(Animal animal, int weight) {
        WeightData current = get(animal);
        if (current.getWeight() == weight) {
            return;
        }
        WeightData replacement = new WeightData(weight, current.getLootPreview());
        replacement.storeBonusCache(current.getBonusCacheTick(), current.getBonusCacheValue());
        ((WeightHolder) animal).animalweights$setWeightData(replacement);
        syncToTrackers(animal);
    }

    public static String getLootPreview(Animal animal) {
        return get(animal).getLootPreview();
    }

    public static void setLootPreview(Animal animal, String csv) {
        WeightData current = get(animal);
        WeightData replacement = new WeightData(current.getWeight(), csv);
        replacement.storeBonusCache(current.getBonusCacheTick(), current.getBonusCacheValue());
        ((WeightHolder) animal).animalweights$setWeightData(replacement);
    }

    public static void syncToTrackers(Animal animal) {
        if (animal.level().isClientSide) {
            return;
        }
        if (!(animal.level() instanceof ServerLevel)) {
            return;
        }
        WeightSyncDispatcher.broadcast(animal);
    }
}

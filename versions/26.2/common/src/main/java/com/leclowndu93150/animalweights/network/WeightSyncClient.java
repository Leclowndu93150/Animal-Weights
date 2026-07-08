package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.WeightAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WeightSyncClient {
    private static final Set<Integer> UNTRACKED = ConcurrentHashMap.newKeySet();

    private WeightSyncClient() {
    }

    public static boolean isTracked(Animal animal) {
        return !UNTRACKED.contains(animal.getId());
    }

    public static void apply(int entityId, int weight, boolean tracked) {
        if (tracked) {
            UNTRACKED.remove(entityId);
        } else {
            UNTRACKED.add(entityId);
        }
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            ClientLevel level = client.level;
            if (level == null) return;
            Entity entity = level.getEntity(entityId);
            if (entity instanceof Animal animal) {
                WeightAttachment.setWeight(animal, weight);
            }
        });
    }
}

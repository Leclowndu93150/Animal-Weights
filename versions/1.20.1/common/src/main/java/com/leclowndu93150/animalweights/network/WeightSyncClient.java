package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.WeightAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

public final class WeightSyncClient {
    private WeightSyncClient() {
    }

    public static void apply(int entityId, int weight) {
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

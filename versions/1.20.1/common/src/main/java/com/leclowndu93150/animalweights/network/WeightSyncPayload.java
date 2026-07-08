package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.Animalweights;
import com.leclowndu93150.animalweights.WeightAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;

public final class WeightSyncPayload {
    public static final ResourceLocation CHANNEL = new ResourceLocation(Animalweights.MOD_ID, "weight_sync");

    public final int entityId;
    public final int weight;
    public final boolean tracked;

    public WeightSyncPayload(int entityId, int weight, boolean tracked) {
        this.entityId = entityId;
        this.weight = weight;
        this.tracked = tracked;
    }

    public static WeightSyncPayload of(Animal animal) {
        return new WeightSyncPayload(animal.getId(), WeightAttachment.getWeight(animal), AnimalWeightsRules.isActive(animal));
    }
}

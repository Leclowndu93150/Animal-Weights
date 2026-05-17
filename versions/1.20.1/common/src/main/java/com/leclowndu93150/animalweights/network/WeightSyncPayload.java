package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.resources.ResourceLocation;

public final class WeightSyncPayload {
    public static final ResourceLocation CHANNEL = new ResourceLocation(Animalweights.MOD_ID, "weight_sync");

    public final int entityId;
    public final int weight;

    public WeightSyncPayload(int entityId, int weight) {
        this.entityId = entityId;
        this.weight = weight;
    }
}

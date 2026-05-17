package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.network.WeightSyncClient;

public final class AnimalweightsForgeClient {
    private AnimalweightsForgeClient() {
    }

    public static void applySync(int entityId, int weight) {
        WeightSyncClient.apply(entityId, weight);
    }
}

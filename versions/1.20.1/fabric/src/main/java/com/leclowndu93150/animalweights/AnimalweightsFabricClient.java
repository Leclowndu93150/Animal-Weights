package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.network.WeightSyncClient;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AnimalweightsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WeightSyncPayload.CHANNEL, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            int weight = buf.readVarInt();
            WeightSyncClient.apply(entityId, weight);
        });
    }
}

package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.Animal;

public class AnimalweightsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Animalweights.init(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            AnimalWeightsCommand.register(dispatcher));

        WeightSyncDispatcher.install(animal -> {
            int weight = WeightAttachment.getWeight(animal);
            for (var player : PlayerLookup.tracking(animal)) {
                ServerPlayNetworking.send(player, WeightSyncPayload.CHANNEL, encode(animal.getId(), weight));
            }
        });
        WeightSyncDispatcher.installPlayer((player, animal) -> {
            int weight = WeightAttachment.getWeight(animal);
            ServerPlayNetworking.send(player, WeightSyncPayload.CHANNEL, encode(animal.getId(), weight));
        });

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof Animal animal) {
                WeightSyncDispatcher.sendTo(player, animal);
            }
        });
    }

    private static FriendlyByteBuf encode(int entityId, int weight) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entityId);
        buf.writeVarInt(weight);
        return buf;
    }
}

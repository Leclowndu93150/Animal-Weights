package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class AnimalweightsForgeNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        WeightSyncPayload.CHANNEL,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private AnimalweightsForgeNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
            0,
            WeightSyncPayload.class,
            AnimalweightsForgeNetwork::encode,
            AnimalweightsForgeNetwork::decode,
            AnimalweightsForgeNetwork::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WeightSyncDispatcher.install(animal -> {
            int weight = WeightAttachment.getWeight(animal);
            CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> animal),
                new WeightSyncPayload(animal.getId(), weight)
            );
        });
        WeightSyncDispatcher.installPlayer((player, animal) -> {
            int weight = WeightAttachment.getWeight(animal);
            CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new WeightSyncPayload(animal.getId(), weight)
            );
        });
    }

    private static void encode(WeightSyncPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.weight);
    }

    private static WeightSyncPayload decode(FriendlyByteBuf buf) {
        return new WeightSyncPayload(buf.readVarInt(), buf.readVarInt());
    }

    private static void handle(WeightSyncPayload payload, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> AnimalweightsForgeClient.applySync(payload.entityId, payload.weight)));
        ctx.get().setPacketHandled(true);
    }
}

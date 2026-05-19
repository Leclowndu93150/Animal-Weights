package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootEntryMessage;
import com.leclowndu93150.animalweights.network.LootSnapshotMessage;
import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            AnimalweightsForgeNetwork::encodeWeight,
            AnimalweightsForgeNetwork::decodeWeight,
            AnimalweightsForgeNetwork::handleWeight,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
            1,
            LootEntryMessage.class,
            AnimalweightsForgeNetwork::encodeLootEntry,
            AnimalweightsForgeNetwork::decodeLootEntry,
            AnimalweightsForgeNetwork::handleLootEntry,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
            2,
            LootSnapshotMessage.class,
            AnimalweightsForgeNetwork::encodeLootSnapshot,
            AnimalweightsForgeNetwork::decodeLootSnapshot,
            AnimalweightsForgeNetwork::handleLootSnapshot,
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
        LootSyncDispatcher.installBroadcaster((type, items) ->
            CHANNEL.send(PacketDistributor.ALL.noArg(), new LootEntryMessage(type, items)));
        LootSyncDispatcher.installSnapshot(player ->
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LootSnapshotMessage(LootCache.snapshot())));
    }

    private static void encodeWeight(WeightSyncPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.weight);
    }

    private static WeightSyncPayload decodeWeight(FriendlyByteBuf buf) {
        return new WeightSyncPayload(buf.readVarInt(), buf.readVarInt());
    }

    private static void handleWeight(WeightSyncPayload payload, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> AnimalweightsForgeClient.applyWeight(payload.entityId, payload.weight)));
        ctx.get().setPacketHandled(true);
    }

    private static void encodeLootEntry(LootEntryMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.entityType);
        buf.writeVarInt(msg.items.size());
        for (ResourceLocation id : msg.items) buf.writeResourceLocation(id);
    }

    private static LootEntryMessage decodeLootEntry(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        int count = buf.readVarInt();
        List<ResourceLocation> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) items.add(buf.readResourceLocation());
        return new LootEntryMessage(type, items);
    }

    private static void handleLootEntry(LootEntryMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> AnimalweightsForgeClient.applyLootEntry(msg.entityType, msg.items)));
        ctx.get().setPacketHandled(true);
    }

    private static void encodeLootSnapshot(LootSnapshotMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries.size());
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : msg.entries.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (ResourceLocation id : entry.getValue()) buf.writeResourceLocation(id);
        }
    }

    private static LootSnapshotMessage decodeLootSnapshot(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, List<ResourceLocation>> entries = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation type = buf.readResourceLocation();
            int n = buf.readVarInt();
            List<ResourceLocation> items = new ArrayList<>(n);
            for (int j = 0; j < n; j++) items.add(buf.readResourceLocation());
            entries.put(type, items);
        }
        return new LootSnapshotMessage(entries);
    }

    private static void handleLootSnapshot(LootSnapshotMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> AnimalweightsForgeClient.applyLootSnapshot(msg.entries)));
        ctx.get().setPacketHandled(true);
    }
}

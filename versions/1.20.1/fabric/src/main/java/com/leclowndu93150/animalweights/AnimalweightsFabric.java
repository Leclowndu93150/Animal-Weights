package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import com.leclowndu93150.animalweights.network.LootSyncPayload;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;
import java.util.Map;

public class AnimalweightsFabric implements ModInitializer {
    private static MinecraftServer activeServer;

    @Override
    public void onInitialize() {
        Animalweights.init(FabricLoader.getInstance().getConfigDir());
        AnimalweightsItems.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            AnimalWeightsCommand.register(dispatcher));

        WeightSyncDispatcher.install(animal -> {
            for (var player : PlayerLookup.tracking(animal)) {
                ServerPlayNetworking.send(player, WeightSyncPayload.CHANNEL, encodeWeight(animal));
            }
        });
        WeightSyncDispatcher.installPlayer((player, animal) ->
            ServerPlayNetworking.send(player, WeightSyncPayload.CHANNEL, encodeWeight(animal)));

        LootSyncDispatcher.installBroadcaster((type, items) -> {
            if (activeServer == null) return;
            for (var player : activeServer.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, LootSyncPayload.ENTRY_CHANNEL, encodeLootEntry(type, items));
            }
        });
        LootSyncDispatcher.installSnapshot(player ->
            ServerPlayNetworking.send(player, LootSyncPayload.SNAPSHOT_CHANNEL, encodeLootSnapshot(LootCache.snapshot())));

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof Animal animal) {
                WeightSyncDispatcher.sendTo(player, animal);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            LootSyncDispatcher.sendSnapshot(handler.player));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ConfigManager.reload());
        ServerLifecycleEvents.SERVER_STARTED.register(server -> activeServer = server);
        ServerTickEvents.END_SERVER_TICK.register(server -> ConfigManager.tickPendingSave());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ConfigManager.saveNow());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            activeServer = null;
            LootCache.clear();
        });
    }

    private static FriendlyByteBuf encodeWeight(Animal animal) {
        WeightSyncPayload payload = WeightSyncPayload.of(animal);
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.weight);
        buf.writeBoolean(payload.tracked);
        return buf;
    }

    private static FriendlyByteBuf encodeLootEntry(ResourceLocation type, List<ResourceLocation> items) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeResourceLocation(type);
        buf.writeVarInt(items.size());
        for (ResourceLocation id : items) buf.writeResourceLocation(id);
        return buf;
    }

    private static FriendlyByteBuf encodeLootSnapshot(Map<ResourceLocation, List<ResourceLocation>> entries) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entries.size());
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : entries.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeVarInt(entry.getValue().size());
            for (ResourceLocation id : entry.getValue()) buf.writeResourceLocation(id);
        }
        return buf;
    }
}

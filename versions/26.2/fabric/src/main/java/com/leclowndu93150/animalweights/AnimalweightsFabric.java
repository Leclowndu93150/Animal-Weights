package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootEntryPayload;
import com.leclowndu93150.animalweights.network.LootSnapshotPayload;
import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.Animal;

public class AnimalweightsFabric implements ModInitializer {
    private static MinecraftServer activeServer;

    @Override
    public void onInitialize() {
        Animalweights.init(FabricLoader.getInstance().getConfigDir());
        AnimalweightsItems.register();

        PayloadTypeRegistry.clientboundPlay().register(WeightSyncPayload.TYPE, WeightSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LootEntryPayload.TYPE, LootEntryPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LootSnapshotPayload.TYPE, LootSnapshotPayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            AnimalWeightsCommand.register(dispatcher));

        WeightSyncDispatcher.install(animal -> {
            WeightSyncPayload payload = WeightSyncPayload.of(animal);
            for (var player : PlayerLookup.tracking(animal)) {
                ServerPlayNetworking.send(player, payload);
            }
        });
        WeightSyncDispatcher.installPlayer((player, animal) ->
            ServerPlayNetworking.send(player, WeightSyncPayload.of(animal)));

        LootSyncDispatcher.installBroadcaster((type, items) -> {
            if (activeServer == null) return;
            LootEntryPayload payload = new LootEntryPayload(type, items);
            for (var player : activeServer.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        });
        LootSyncDispatcher.installSnapshot(player ->
            ServerPlayNetworking.send(player, new LootSnapshotPayload(LootCache.snapshot())));

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
}

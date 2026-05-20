package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootEntryPayload;
import com.leclowndu93150.animalweights.network.LootSnapshotPayload;
import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("animalweights")
public class AnimalweightsNeoForge {
    public AnimalweightsNeoForge(IEventBus modBus) {
        Animalweights.init(FMLPaths.CONFIGDIR.get());
        AnimalweightsItems.register(modBus);
        modBus.addListener(AnimalweightsNeoForge::onRegisterPayloads);
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForge::onStartTracking);
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForge::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(AnimalweightsNeoForge::onServerStopped);

        WeightSyncDispatcher.install(animal ->
            PacketDistributor.sendToPlayersTrackingEntity(animal,
                new WeightSyncPayload(animal.getId(), WeightAttachment.getWeight(animal))));
        WeightSyncDispatcher.installPlayer((player, animal) ->
            PacketDistributor.sendToPlayer(player,
                new WeightSyncPayload(animal.getId(), WeightAttachment.getWeight(animal))));
        LootSyncDispatcher.installBroadcaster((type, items) ->
            PacketDistributor.sendToAllPlayers(new LootEntryPayload(type, items)));
        LootSyncDispatcher.installSnapshot(player ->
            PacketDistributor.sendToPlayer(player, new LootSnapshotPayload(LootCache.snapshot())));
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(WeightSyncPayload.TYPE, WeightSyncPayload.CODEC);
        registrar.playToClient(LootEntryPayload.TYPE, LootEntryPayload.CODEC);
        registrar.playToClient(LootSnapshotPayload.TYPE, LootSnapshotPayload.CODEC);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AnimalWeightsCommand.register(event.getDispatcher());
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Animal animal && event.getEntity() instanceof ServerPlayer player) {
            WeightSyncDispatcher.sendTo(player, animal);
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LootSyncDispatcher.sendSnapshot(player);
        }
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        LootCache.clear();
    }
}

package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.display.LootCache;
import com.leclowndu93150.animalweights.network.LootSyncDispatcher;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("animalweights")
public class AnimalweightsForge {
    public AnimalweightsForge() {
        Animalweights.init(FMLPaths.CONFIGDIR.get());
        AnimalweightsItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        AnimalweightsForgeNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onServerStopped);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AnimalweightsForge::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AnimalweightsForgeClient::init);
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

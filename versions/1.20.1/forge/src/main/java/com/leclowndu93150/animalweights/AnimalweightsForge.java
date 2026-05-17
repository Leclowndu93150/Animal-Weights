package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import com.leclowndu93150.animalweights.network.WeightSyncDispatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("animalweights")
public class AnimalweightsForge {
    public AnimalweightsForge() {
        Animalweights.init(FMLPaths.CONFIGDIR.get());
        AnimalweightsForgeNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onStartTracking);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AnimalWeightsCommand.register(event.getDispatcher());
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Animal animal && event.getEntity() instanceof ServerPlayer player) {
            WeightSyncDispatcher.sendTo(player, animal);
        }
    }
}

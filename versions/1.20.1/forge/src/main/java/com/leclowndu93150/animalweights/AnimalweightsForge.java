package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("animalweights")
public class AnimalweightsForge {
    public AnimalweightsForge() {
        Animalweights.init(FMLPaths.CONFIGDIR.get());
        MinecraftForge.EVENT_BUS.addListener(AnimalweightsForge::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AnimalWeightsCommand.register(event.getDispatcher());
    }
}

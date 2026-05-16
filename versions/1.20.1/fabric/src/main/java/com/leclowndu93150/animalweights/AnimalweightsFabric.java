package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.command.AnimalWeightsCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class AnimalweightsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Animalweights.init(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
            AnimalWeightsCommand.register(dispatcher));
    }
}

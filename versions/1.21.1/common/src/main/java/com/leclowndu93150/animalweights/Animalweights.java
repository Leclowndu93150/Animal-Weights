package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Animalweights {
    public static final String MOD_ID = "animalweights";
    public static final String MOD_NAME = "Animal Weights";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init(Path configDir) {
        LOGGER.info("{} initializing!", MOD_NAME);
        ConfigManager.loadOrCreate(configDir, (msg, err) -> LOGGER.error(msg, err));
    }
}

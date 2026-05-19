package com.leclowndu93150.animalweights.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static AnimalWeightsConfig active = new AnimalWeightsConfig();

    private ConfigManager() {
    }

    public static AnimalWeightsConfig get() {
        return active;
    }

    public static void loadOrCreate(Path configDir, java.util.function.BiConsumer<String, Throwable> errorLogger) {
        Path file = configDir.resolve("animalweights.json");
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file)) {
                    AnimalWeightsConfig parsed = GSON.fromJson(r, AnimalWeightsConfig.class);
                    if (parsed != null) {
                        active = sanitize(parsed);
                        return;
                    }
                } catch (JsonSyntaxException e) {
                    errorLogger.accept("animalweights.json is invalid, using defaults", e);
                }
            }
            Files.createDirectories(configDir);
            active = new AnimalWeightsConfig();
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(active, w);
            }
        } catch (IOException e) {
            errorLogger.accept("Failed to load animalweights.json", e);
        }
    }

    private static AnimalWeightsConfig sanitize(AnimalWeightsConfig c) {
        if (c.dropScalingMode == null) c.dropScalingMode = ScalingMode.MULTIPLICATIVE;
        if (c.xpScalingMode == null) c.xpScalingMode = ScalingMode.MULTIPLICATIVE;
        if (c.overlayMode == null) c.overlayMode = OverlayMode.LOOKING;
        if (c.overlayRange < 1.0) c.overlayRange = 1.0;
        if (c.overlayRange > 64.0) c.overlayRange = 64.0;
        if (c.minWeight < 0) c.minWeight = 0;
        if (c.maxWeight < c.minWeight + 1) c.maxWeight = c.minWeight + 1;
        if (c.defaultWeight < c.minWeight) c.defaultWeight = c.minWeight;
        if (c.defaultWeight > c.maxWeight) c.defaultWeight = c.maxWeight;
        if (c.weightTickIntervalTicks < 20) c.weightTickIntervalTicks = 20;
        if (c.habitatScanRadius < 1) c.habitatScanRadius = 1;
        if (c.crowdRadius < 1) c.crowdRadius = 1;
        if (c.crowdLimit < 1) c.crowdLimit = 1;
        if (c.proximityRadius < 1) c.proximityRadius = 1;
        if (c.lightThreshold < 0) c.lightThreshold = 0;
        if (c.lightThreshold > 15) c.lightThreshold = 15;
        return c;
    }
}

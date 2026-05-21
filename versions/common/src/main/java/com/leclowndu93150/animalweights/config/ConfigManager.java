package com.leclowndu93150.animalweights.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
                    JsonObject json = JsonParser.parseReader(r).getAsJsonObject();
                    AnimalWeightsConfig parsed = GSON.fromJson(json, AnimalWeightsConfig.class);
                    if (parsed != null) {
                        active = sanitize(parsed);
                        mergeAndSave(file, json, active);
                        return;
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    errorLogger.accept("animalweights.json is invalid, using defaults", e);
                }
            }
            Files.createDirectories(configDir);
            active = new AnimalWeightsConfig();
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(commentedConfig(active), w);
            }
        } catch (IOException e) {
            errorLogger.accept("Failed to load animalweights.json", e);
        }
    }

    private static void mergeAndSave(Path file, JsonObject existing, AnimalWeightsConfig config) throws IOException {
        JsonObject sanitized = commentedConfig(config);
        boolean changed = false;
        for (Map.Entry<String, JsonElement> entry : sanitized.entrySet()) {
            JsonElement current = existing.get(entry.getKey());
            if (current == null || !current.equals(entry.getValue())) {
                existing.add(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        if (changed) {
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(existing, w);
            }
        }
    }

    private static JsonObject commentedConfig(AnimalWeightsConfig config) {
        JsonObject values = GSON.toJsonTree(config).getAsJsonObject();
        JsonObject out = new JsonObject();

        addComment(out, "dropScalingMode", "How animal weight changes loot drops. Possible inputs: MULTIPLICATIVE, ADDITIVE.");
        addValue(out, values, "dropScalingMode");
        addComment(out, "xpScalingMode", "How animal weight changes XP drops. Possible inputs: MULTIPLICATIVE, ADDITIVE.");
        addValue(out, values, "xpScalingMode");

        addComment(out, "minWeight", "Lowest weight an adult animal can have. At 0 or below the animal is treated as sick.");
        addValue(out, values, "minWeight");
        addComment(out, "defaultWeight", "Starting weight for animals that do not have saved weight data yet.");
        addValue(out, values, "defaultWeight");
        addComment(out, "maxWeight", "Highest weight an animal can reach.");
        addValue(out, values, "maxWeight");

        addComment(out, "weightTickIntervalTicks", "How often animal weights are rechecked, in game ticks. 20 ticks = 1 second.");
        addValue(out, values, "weightTickIntervalTicks");
        addComment(out, "weightGainChance", "Chance from 0.0 to 1.0 for a suitable habitat check to increase weight.");
        addValue(out, values, "weightGainChance");
        addComment(out, "weightMinorLossChance", "Chance from 0.0 to 1.0 for a poor habitat check to lose one weight.");
        addValue(out, values, "weightMinorLossChance");
        addComment(out, "weightSevereLossChance", "Chance from 0.0 to 1.0 for a very poor habitat check to lose extra weight.");
        addValue(out, values, "weightSevereLossChance");

        addComment(out, "lightThreshold", "Minimum block light level, from 0 to 15, counted as a good habitat.");
        addValue(out, values, "lightThreshold");
        addComment(out, "habitatScanRadius", "Radius in blocks used to scan nearby habitat conditions.");
        addValue(out, values, "habitatScanRadius");
        addComment(out, "crowdRadius", "Radius in blocks used to count nearby animals for crowding.");
        addValue(out, values, "crowdRadius");
        addComment(out, "crowdLimit", "Maximum nearby animals allowed before crowding can hurt weight.");
        addValue(out, values, "crowdLimit");

        addComment(out, "enableProximityBonus", "Whether nearby water or matching habitat blocks can help animal weight. Possible inputs: true, false.");
        addValue(out, values, "enableProximityBonus");
        addComment(out, "proximityRadius", "Radius in blocks used for proximity habitat checks.");
        addValue(out, values, "proximityRadius");

        addComment(out, "enableSickTint", "Whether sick animals get a green tint. Possible inputs: true, false.");
        addValue(out, values, "enableSickTint");
        addComment(out, "enableSickParticles", "Whether sick animals emit particles. Possible inputs: true, false.");
        addValue(out, values, "enableSickParticles");
        addComment(out, "enableOverlay", "Whether floating item and text overlays are rendered on the client. Possible inputs: true, false.");
        addValue(out, values, "enableOverlay");
        addComment(out, "sickTintColor", "ARGB color for sick tint as a JSON integer. Example default is 0xFF80C070 as a signed decimal integer.");
        addValue(out, values, "sickTintColor");

        addComment(out, "overlayMode", "When overlays are shown. Possible inputs: ALWAYS, LOOKING, CROUCH_LOOKING, NEARBY.");
        addValue(out, values, "overlayMode");
        addComment(out, "overlayRange", "Maximum distance in blocks for overlay visibility. Clamped from 1.0 to 64.0.");
        addValue(out, values, "overlayRange");

        addComment(out, "pauseAtNight", "Whether animals pause their weight cycle at night, like sleeping. Possible inputs: true, false.");
        addValue(out, values, "pauseAtNight");

        addComment(out, "defaultDiet", "Diet used for animals not listed in entityDiets (modded animals). Possible inputs: HERBIVORE, CARNIVORE, OMNIVORE, AQUATIC.");
        addValue(out, values, "defaultDiet");
        addComment(out, "entityDiets", "Diet per entity type. Herbivores need grazing, aquatic need water, carnivores skip grazing, omnivores skip both grazing and water requirements (still benefit from them).");
        addValue(out, values, "entityDiets");
        addComment(out, "disabledEntities", "Entity type IDs (e.g. \"quark:shiba\") fully ignored by the mod: no weight tracking, no drop scaling, no sick tint, no breeding block, no tooltip.");
        addValue(out, values, "disabledEntities");

        return out;
    }

    private static void addComment(JsonObject object, String key, String text) {
        object.addProperty("_comment_" + key, text);
    }

    private static void addValue(JsonObject object, JsonObject values, String key) {
        object.add(key, values.get(key));
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
        if (c.defaultDiet == null) c.defaultDiet = Diet.OMNIVORE;
        if (c.entityDiets == null) c.entityDiets = new java.util.HashMap<>();
        if (c.disabledEntities == null) c.disabledEntities = new java.util.LinkedHashSet<>();
        return c;
    }
}

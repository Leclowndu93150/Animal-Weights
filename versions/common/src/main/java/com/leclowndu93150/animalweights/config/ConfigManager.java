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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final long SAVE_DEBOUNCE_NANOS = 5_000_000_000L;
    private static final Set<String> NETHER_TAGGED = Set.of(
        "minecraft:strider"
    );
    private static final Set<String> DEFAULT_DISABLED = Set.of(
        "minecraft:happy_ghast"
    );
    private static AnimalWeightsConfig active = new AnimalWeightsConfig();
    private static Path activePath;
    private static BiConsumer<String, Throwable> activeErrorLogger = (msg, t) -> {};
    private static final AtomicLong pendingSaveDeadline = new AtomicLong(0L);

    private ConfigManager() {
    }

    public static AnimalWeightsConfig get() {
        return active;
    }

    public static void scheduleSave() {
        if (activePath == null) {
            return;
        }
        pendingSaveDeadline.set(System.nanoTime() + SAVE_DEBOUNCE_NANOS);
    }

    public static void tickPendingSave() {
        long deadline = pendingSaveDeadline.get();
        if (deadline == 0L || System.nanoTime() < deadline) {
            return;
        }
        if (!pendingSaveDeadline.compareAndSet(deadline, 0L)) {
            return;
        }
        saveNow();
    }

    public static void saveNow() {
        if (activePath == null) {
            return;
        }
        pendingSaveDeadline.set(0L);
        try {
            Files.createDirectories(activePath.getParent());
            try (Writer w = Files.newBufferedWriter(activePath)) {
                GSON.toJson(commentedConfig(active), w);
            }
        } catch (IOException e) {
            activeErrorLogger.accept("Failed to save animalweights.json", e);
        }
    }

    public static void loadOrCreate(Path configDir, java.util.function.BiConsumer<String, Throwable> errorLogger) {
        Path file = configDir.resolve("animalweights.json");
        activePath = file;
        activeErrorLogger = errorLogger;
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
            active = sanitize(new AnimalWeightsConfig());
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

        addComment(out, "naturalSpawnSicknessResistance", "Whether naturally-spawned animals (from chunk gen, structures, patrols) are more resistant to losing weight from a bad habitat. Halves the chance of every weight-loss roll. Bred animals, eggs, and spawners are unaffected. Possible inputs: true, false.");
        addValue(out, values, "naturalSpawnSicknessResistance");
        addComment(out, "cauldronCountsAsWater", "Whether a water cauldron near an animal satisfies the water habitat requirement. Each successful weight gain that relied on a cauldron drains one fill level; an empty cauldron must be refilled by hand. Lets you keep livestock in the Nether or anywhere away from natural water. Possible inputs: true, false.");
        addValue(out, values, "cauldronCountsAsWater");
        addComment(out, "cauldronScanRadius", "Radius in blocks used to find a water cauldron around the animal. Defaults to 8 (larger than habitatScanRadius) so one cauldron covers a small barn.");
        addValue(out, values, "cauldronScanRadius");

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

        addComment(out, "defaultDiet", "Diet used for animals not listed in entityDiets (modded animals). Possible inputs: HERBIVORE, CARNIVORE, OMNIVORE, AQUATIC, NETHER.");
        addValue(out, values, "defaultDiet");
        addComment(out, "entityDiets", "Per-entity-type diet overrides. Starts empty; auto-populated from #animalweights:diet/* tags and biome spawn data on first sighting. Edit entries here to override. Herbivores need grazing, aquatic need water, carnivores skip grazing, omnivores skip both. NETHER animals (e.g. Strider) need lava and nylium/netherrack instead of water and grass, and only gain weight while in the Nether dimension; non-NETHER animals brought into the Nether have their weight cycle paused. Possible inputs: HERBIVORE, CARNIVORE, OMNIVORE, AQUATIC, NETHER.");
        addValue(out, values, "entityDiets");
        addComment(out, "entityFilterMode", "How the mod decides which entities to track. BLACKLIST: every Animal is tracked except those in disabledEntities. WHITELIST: only entities in enabledEntities are tracked. VANILLA_ONLY: track only minecraft: entities (plus anything in enabledEntities, minus anything in disabledEntities). Possible inputs: BLACKLIST, WHITELIST, VANILLA_ONLY.");
        addValue(out, values, "entityFilterMode");
        addComment(out, "disabledEntities", "Entity type IDs (e.g. \"quark:shiba\") fully ignored by the mod: no weight tracking, no drop scaling, no sick tint, no breeding block, no tooltip. Used in BLACKLIST and VANILLA_ONLY modes.");
        addValue(out, values, "disabledEntities");
        addComment(out, "enabledEntities", "Entity type IDs that the mod tracks. Used in WHITELIST mode (only these are tracked) and as an extra opt-in list in VANILLA_ONLY mode. Example: [\"minecraft:cow\", \"minecraft:pig\", \"minecraft:chicken\"].");
        addValue(out, values, "enabledEntities");

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
        if (c.cauldronScanRadius < 1) c.cauldronScanRadius = 1;
        if (c.cauldronScanRadius > 32) c.cauldronScanRadius = 32;
        if (c.lightThreshold < 0) c.lightThreshold = 0;
        if (c.lightThreshold > 15) c.lightThreshold = 15;
        if (c.defaultDiet == null) c.defaultDiet = Diet.OMNIVORE;
        if (c.entityDiets == null) c.entityDiets = new java.util.HashMap<>();
        if (c.entityFilterMode == null) c.entityFilterMode = EntityFilterMode.BLACKLIST;
        if (c.disabledEntities == null) c.disabledEntities = new java.util.LinkedHashSet<>();
        if (c.enabledEntities == null) c.enabledEntities = new java.util.LinkedHashSet<>();
        for (String id : NETHER_TAGGED) {
            Diet existing = c.entityDiets.get(id);
            if (existing != null && existing != Diet.NETHER) {
                c.entityDiets.put(id, Diet.NETHER);
            }
        }
        c.disabledEntities.addAll(DEFAULT_DISABLED);
        return c;
    }
}

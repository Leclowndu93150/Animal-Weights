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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final long SAVE_DEBOUNCE_NANOS = 5_000_000_000L;
    private static final String COMMENT_PREFIX = "_comment";
    private static final Set<String> NETHER_TAGGED = Set.of(
        "minecraft:strider"
    );
    private static final Set<String> DEFAULT_DISABLED = Set.of(
        "minecraft:happy_ghast"
    );
    private static AnimalWeightsConfig active = new AnimalWeightsConfig();
    private static int generation = 0;
    private static Path activePath;
    private static Path dietsCachePath;
    private static BiConsumer<String, Throwable> activeErrorLogger = (msg, t) -> {};
    private static final AtomicLong pendingSaveDeadline = new AtomicLong(0L);

    private ConfigManager() {
    }

    public static AnimalWeightsConfig get() {
        return active;
    }

    public static int generation() {
        return generation;
    }

    public static WeightStats statsFor(String entityId) {
        AnimalWeightsConfig config = active;
        AnimalProfile profile = config.animalProfiles.get(entityId);
        return profile == null ? config.baseStats() : profile.stats(config);
    }

    public static void scheduleSave() {
        if (dietsCachePath == null) {
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
        saveDietsCache();
    }

    public static void saveNow() {
        if (activePath == null) {
            return;
        }
        pendingSaveDeadline.set(0L);
        saveMainConfig();
        saveDietsCache();
    }

    public static void loadOrCreate(Path configDir, BiConsumer<String, Throwable> errorLogger) {
        activePath = configDir.resolve("animalweights.json");
        dietsCachePath = configDir.resolve("animaldiets.json");
        activeErrorLogger = errorLogger;
        load();
    }

    public static void reload() {
        if (activePath == null) {
            return;
        }
        pendingSaveDeadline.set(0L);
        load();
    }

    private static void load() {
        BiConsumer<String, Throwable> errorLogger = activeErrorLogger;
        AnimalWeightsConfig parsed = null;
        JsonObject json = null;
        try {
            if (Files.exists(activePath)) {
                try (Reader r = Files.newBufferedReader(activePath)) {
                    json = JsonParser.parseReader(r).getAsJsonObject();
                    ConfigMigrations.migrateMainConfig(json);
                    parsed = GSON.fromJson(json, AnimalWeightsConfig.class);
                } catch (JsonSyntaxException | IllegalStateException e) {
                    errorLogger.accept("animalweights.json is invalid, using defaults", e);
                }
            }
        } catch (IOException e) {
            errorLogger.accept("Failed to load animalweights.json", e);
        }
        if (parsed == null) {
            parsed = new AnimalWeightsConfig();
        }

        JsonElement legacyDiets = json != null ? json.get("entityDiets") : null;
        parsed.animalProfiles = loadProfiles(legacyDiets, errorLogger);

        active = sanitize(parsed);
        generation++;
        saveMainConfig();
        saveDietsCache();
    }

    private static Map<String, AnimalProfile> loadProfiles(JsonElement legacyDiets, BiConsumer<String, Throwable> errorLogger) {
        if (Files.exists(dietsCachePath)) {
            try (Reader r = Files.newBufferedReader(dietsCachePath)) {
                return parseProfiles(JsonParser.parseReader(r), errorLogger);
            } catch (JsonSyntaxException | IllegalStateException | IOException e) {
                errorLogger.accept("animaldiets.json is invalid, using empty cache", e);
                return new ConcurrentHashMap<>();
            }
        }
        return parseProfiles(legacyDiets, errorLogger);
    }

    private static Map<String, AnimalProfile> parseProfiles(JsonElement root, BiConsumer<String, Throwable> errorLogger) {
        Map<String, AnimalProfile> profiles = new ConcurrentHashMap<>();
        if (root == null || !root.isJsonObject()) {
            return profiles;
        }
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
            if (entry.getKey().startsWith(COMMENT_PREFIX)) {
                continue;
            }
            JsonObject migrated = ConfigMigrations.migrateProfileEntry(entry.getValue());
            if (migrated == null) {
                continue;
            }
            try {
                AnimalProfile profile = GSON.fromJson(migrated, AnimalProfile.class);
                if (profile != null) {
                    profiles.put(entry.getKey(), profile);
                }
            } catch (JsonSyntaxException e) {
                errorLogger.accept("animaldiets.json entry " + entry.getKey() + " is invalid, skipping it", e);
            }
        }
        return profiles;
    }

    private static void saveMainConfig() {
        if (activePath == null) {
            return;
        }
        try {
            Files.createDirectories(activePath.getParent());
            try (Writer w = Files.newBufferedWriter(activePath)) {
                GSON.toJson(commentedConfig(active), w);
            }
        } catch (IOException e) {
            activeErrorLogger.accept("Failed to save animalweights.json", e);
        }
    }

    private static void saveDietsCache() {
        if (dietsCachePath == null) {
            return;
        }
        try {
            Files.createDirectories(dietsCachePath.getParent());
            try (Writer w = Files.newBufferedWriter(dietsCachePath)) {
                GSON.toJson(commentedProfiles(active.animalProfiles), w);
            }
        } catch (IOException e) {
            activeErrorLogger.accept("Failed to save animaldiets.json", e);
        }
    }

    private static JsonObject commentedProfiles(Map<String, AnimalProfile> profiles) {
        JsonObject out = new JsonObject();
        out.addProperty(COMMENT_PREFIX, "One entry per animal, keyed by entity ID (e.g. \"minecraft:cow\"). New animals are added with their detected diet the first time they are seen. Every key inside an entry is optional: anything left out uses the value from animalweights.json. Example: \"minecraft:strider\": {\"diet\": \"NETHER\", \"maxWeight\": 4, \"weightGainChance\": 0.25}.");
        addComment(out, "diet", "What the animal needs nearby to gain weight, on top of light and open space. HERBIVORE: water and grazing ground. CARNIVORE: water. OMNIVORE: water or grazing ground. AQUATIC: water. NETHER: lava and nylium or netherrack (light is optional), and it only gains weight in the Nether unless netherAnimalsGainAnywhere is on. Leave it out to let the mod detect it. Possible inputs: HERBIVORE, CARNIVORE, OMNIVORE, AQUATIC, NETHER.");
        addComment(out, "minWeight", "Lowest weight this animal can drop to. Whole number, 0 or higher.");
        addComment(out, "maxWeight", "Highest weight this animal can reach. Higher weight means more drops and XP. Whole number, at least minWeight + 1.");
        addComment(out, "defaultWeight", "Weight this animal starts at. Whole number between minWeight and maxWeight.");
        addComment(out, "sickThreshold", "At or below this weight the animal is sick: it gets a tint and particles, and cannot breed. Whole number.");
        addComment(out, "weightTickIntervalTicks", "How often this animal's weight is rechecked, in game ticks. 20 ticks = 1 second. Minimum 20.");
        addComment(out, "weightGainChance", "Chance from 0.0 to 1.0 for this animal to gain weight when its habitat is good.");
        addComment(out, "weightMinorLossChance", "Chance from 0.0 to 1.0 for this animal to lose weight when its habitat is poor.");
        addComment(out, "weightSevereLossChance", "Chance from 0.0 to 1.0 for this animal to lose weight when its habitat is very poor.");
        for (Map.Entry<String, AnimalProfile> entry : new TreeMap<>(profiles).entrySet()) {
            out.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
        }
        return out;
    }

    private static JsonObject commentedConfig(AnimalWeightsConfig config) {
        JsonObject values = GSON.toJsonTree(config).getAsJsonObject();
        JsonObject out = new JsonObject();

        addComment(out, "configVersion", "Config format version. Used to upgrade older configs automatically when a new version of the mod changes the format. Do not edit.");
        addValue(out, values, "configVersion");

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
        addComment(out, "sickThreshold", "Animals at or below this weight are sick: they get a tint and particles, and cannot breed.");
        addValue(out, values, "sickThreshold");

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

        addComment(out, "requireOpenSpace", "Whether animals need open space around them to count a habitat as good. Stops a mob sealed in a one-block hole from gaining weight. Possible inputs: true, false.");
        addValue(out, values, "requireOpenSpace");
        addComment(out, "minOpenSpace", "Minimum number of empty blocks within habitatScanRadius the animal must have around it when requireOpenSpace is on.");
        addValue(out, values, "minOpenSpace");
        addComment(out, "grazingBlocks", "Extra block IDs that count as grazing ground in addition to vanilla grass and moss. Example: [\"tfc:grass/loam\", \"tfc:grass/sandy_loam\"].");
        addValue(out, values, "grazingBlocks");
        addComment(out, "grazingBlockTags", "Block tag IDs that count as grazing ground in addition to vanilla grass and moss. Example: [\"c:grass_blocks\"].");
        addValue(out, values, "grazingBlockTags");

        addComment(out, "enableProximityBonus", "Whether nearby water or matching habitat blocks can help animal weight. Possible inputs: true, false.");
        addValue(out, values, "enableProximityBonus");
        addComment(out, "proximityRadius", "Radius in blocks used for proximity habitat checks.");
        addValue(out, values, "proximityRadius");

        addComment(out, "requireEngagement", "Whether naturally-spawned wild animals are ignored by the mod (no weight, no loot scaling, vanilla behavior) until a player engages with them by leashing or breeding them. Keeps wild herds cheap to run and stops buffed loot from animals you never raised. Bred, spawn-egg, and spawner animals are always tracked. Possible inputs: true, false.");
        addValue(out, values, "requireEngagement");
        addComment(out, "naturalSpawnSicknessResistance", "Whether naturally-spawned animals (from chunk gen, structures, patrols) are more resistant to losing weight from a bad habitat. Halves the chance of every weight-loss roll. Bred animals, eggs, and spawners are unaffected. Possible inputs: true, false.");
        addValue(out, values, "naturalSpawnSicknessResistance");
        addComment(out, "waterSource", "What counts as water for the water habitat requirement. ANY: natural water or a water cauldron. NATURAL_ONLY: only water blocks, cauldrons are ignored. CAULDRON_ONLY: only water cauldrons, water blocks are ignored (AQUATIC animals still use the water they live in). Each successful weight gain that relied on a cauldron drains one fill level; an empty cauldron must be refilled by hand. Possible inputs: ANY, NATURAL_ONLY, CAULDRON_ONLY.");
        addValue(out, values, "waterSource");
        addComment(out, "feedingTroughCountsAsGrazing", "Whether a feeding trough from the Animal Feeding Trough mod that holds food the animal eats satisfies the grazing requirement. Each successful weight gain that relied on a trough eats one item from it. Has no effect without that mod. Possible inputs: true, false.");
        addValue(out, values, "feedingTroughCountsAsGrazing");
        addComment(out, "feederScanRadius", "Radius in blocks used to find a water cauldron or feeding trough around the animal. Defaults to 8 (larger than habitatScanRadius) so one cauldron or trough covers a small barn.");
        addValue(out, values, "feederScanRadius");

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
        addComment(out, "netherAnimalsGainAnywhere", "Whether NETHER-diet animals (e.g. Striders) can gain weight in any dimension instead of only the Nether. When on, they use their lava + nylium habitat check wherever they are, so you can keep them in an Overworld pen. Possible inputs: true, false.");
        addValue(out, values, "netherAnimalsGainAnywhere");

        addComment(out, "defaultDiet", "Diet used for animals not listed in animaldiets.json (modded animals). Possible inputs: HERBIVORE, CARNIVORE, OMNIVORE, AQUATIC, NETHER.");
        addValue(out, values, "defaultDiet");
        addComment(out, "animaldiets", "Per-animal diets and stat overrides live in animaldiets.json. The comments at the top of that file explain every option.");
        addComment(out, "entityFilterMode", "How the mod decides which entities to track. BLACKLIST: every Animal is tracked except those in disabledEntities. WHITELIST: only entities in enabledEntities are tracked. VANILLA_ONLY: track only minecraft: entities (plus anything in enabledEntities, minus anything in disabledEntities). LIVESTOCK: track only farm animals in the animalweights:livestock entity tag (cow, mooshroom, pig, sheep, chicken, goat, rabbit; extend it with a datapack), plus anything in enabledEntities, minus anything in disabledEntities. Possible inputs: BLACKLIST, WHITELIST, VANILLA_ONLY, LIVESTOCK.");
        addValue(out, values, "entityFilterMode");
        addComment(out, "disabledEntities", "Entity type IDs (e.g. \"quark:shiba\") fully ignored by the mod: no weight tracking, no drop scaling, no sick tint, no breeding block, no tooltip. Used in BLACKLIST, VANILLA_ONLY and LIVESTOCK modes.");
        addValue(out, values, "disabledEntities");
        addComment(out, "enabledEntities", "Entity type IDs that the mod tracks. Used in WHITELIST mode (only these are tracked) and as an extra opt-in list in VANILLA_ONLY and LIVESTOCK modes. Example: [\"minecraft:cow\", \"minecraft:pig\", \"minecraft:chicken\"].");
        addValue(out, values, "enabledEntities");

        return out;
    }

    private static void addComment(JsonObject object, String key, String text) {
        object.addProperty(COMMENT_PREFIX + "_" + key, text);
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
        if (c.minOpenSpace < 0) c.minOpenSpace = 0;
        if (c.grazingBlocks == null) c.grazingBlocks = new LinkedHashSet<>();
        if (c.grazingBlockTags == null) c.grazingBlockTags = new LinkedHashSet<>();
        if (c.proximityRadius < 1) c.proximityRadius = 1;
        if (c.waterSource == null) c.waterSource = WaterSource.ANY;
        if (c.feederScanRadius < 1) c.feederScanRadius = 1;
        if (c.feederScanRadius > 32) c.feederScanRadius = 32;
        if (c.lightThreshold < 0) c.lightThreshold = 0;
        if (c.lightThreshold > 15) c.lightThreshold = 15;
        if (c.defaultDiet == null) c.defaultDiet = Diet.OMNIVORE;
        if (c.animalProfiles == null) c.animalProfiles = new ConcurrentHashMap<>();
        if (c.entityFilterMode == null) c.entityFilterMode = EntityFilterMode.BLACKLIST;
        if (c.disabledEntities == null) c.disabledEntities = new LinkedHashSet<>();
        if (c.enabledEntities == null) c.enabledEntities = new LinkedHashSet<>();
        for (String id : NETHER_TAGGED) {
            AnimalProfile existing = c.animalProfiles.get(id);
            if (existing != null && existing.diet != null && existing.diet != Diet.NETHER) {
                existing.diet = Diet.NETHER;
            }
        }
        c.disabledEntities.addAll(DEFAULT_DISABLED);
        return c;
    }
}

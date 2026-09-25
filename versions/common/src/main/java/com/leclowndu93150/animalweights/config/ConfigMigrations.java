package com.leclowndu93150.animalweights.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.function.Consumer;

final class ConfigMigrations {
    private static final List<Consumer<JsonObject>> MAIN_CONFIG_STEPS = List.of(
        ConfigMigrations::waterSourceAndFeederRadius
    );
    static final int CURRENT_VERSION = MAIN_CONFIG_STEPS.size() + 1;

    private ConfigMigrations() {
    }

    static void migrateMainConfig(JsonObject json) {
        int version = readVersion(json);
        for (int step = version; step < CURRENT_VERSION; step++) {
            MAIN_CONFIG_STEPS.get(step - 1).accept(json);
        }
        json.addProperty("configVersion", Math.max(version, CURRENT_VERSION));
    }

    static JsonObject migrateProfileEntry(JsonElement entry) {
        if (entry == null || entry.isJsonNull()) {
            return null;
        }
        if (entry.isJsonObject()) {
            return entry.getAsJsonObject();
        }
        if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
            JsonObject profile = new JsonObject();
            profile.add("diet", entry);
            return profile;
        }
        return null;
    }

    private static int readVersion(JsonObject json) {
        JsonElement element = json.get("configVersion");
        if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return Math.max(1, primitive.getAsInt());
        }
        return 1;
    }

    private static void waterSourceAndFeederRadius(JsonObject json) {
        JsonElement cauldronCountsAsWater = json.remove("cauldronCountsAsWater");
        if (cauldronCountsAsWater instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            json.addProperty("waterSource", primitive.getAsBoolean() ? WaterSource.ANY.name() : WaterSource.NATURAL_ONLY.name());
        }
        JsonElement cauldronScanRadius = json.remove("cauldronScanRadius");
        if (cauldronScanRadius != null && !json.has("feederScanRadius")) {
            json.add("feederScanRadius", cauldronScanRadius);
        }
        json.remove("_comment_cauldronCountsAsWater");
        json.remove("_comment_cauldronScanRadius");
    }
}

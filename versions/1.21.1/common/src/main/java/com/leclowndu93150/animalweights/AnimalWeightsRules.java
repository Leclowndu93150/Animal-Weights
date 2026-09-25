package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalProfile;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import com.leclowndu93150.animalweights.config.EntityFilterMode;
import com.leclowndu93150.animalweights.config.WeightStats;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public final class AnimalWeightsRules {
    private AnimalWeightsRules() {
    }

    public static boolean isActive(Animal animal) {
        if (!ConfigManager.get().requireEngagement) {
            return true;
        }
        WeightData data = WeightAttachment.get(animal);
        if (!data.isNaturallySpawned() || data.isEngaged()) {
            return true;
        }
        if (animal.isLeashed()) {
            data.setEngaged(true);
            WeightAttachment.syncToTrackers(animal);
            return true;
        }
        return false;
    }

    public static void markEngaged(Animal animal) {
        WeightData data = WeightAttachment.get(animal);
        if (!data.isEngaged()) {
            data.setEngaged(true);
            WeightAttachment.syncToTrackers(animal);
        }
    }

    public static boolean isDisabled(Entity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        String id = idOf(entity);
        return switch (cfg.entityFilterMode == null ? EntityFilterMode.BLACKLIST : cfg.entityFilterMode) {
            case WHITELIST -> cfg.enabledEntities == null || !cfg.enabledEntities.contains(id);
            case VANILLA_ONLY -> {
                if (cfg.disabledEntities != null && cfg.disabledEntities.contains(id)) yield true;
                if (cfg.enabledEntities != null && cfg.enabledEntities.contains(id)) yield false;
                yield !id.startsWith("minecraft:");
            }
            case LIVESTOCK -> {
                if (cfg.disabledEntities != null && cfg.disabledEntities.contains(id)) yield true;
                if (cfg.enabledEntities != null && cfg.enabledEntities.contains(id)) yield false;
                yield !DietResolver.isLivestock(entity.getType());
            }
            case BLACKLIST -> cfg.disabledEntities != null && cfg.disabledEntities.contains(id);
        };
    }

    public static Diet dietOf(Entity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        String id = idOf(entity);
        AnimalProfile profile = cfg.animalProfiles.get(id);
        if (profile != null && profile.diet != null) {
            return profile.diet;
        }
        Diet resolved = DietResolver.resolveByTag(entity.getType());
        if (resolved == null) {
            resolved = DietResolver.resolveBySpawnBiomes(entity);
        }
        if (resolved == null) {
            resolved = cfg.defaultDiet != null ? cfg.defaultDiet : Diet.OMNIVORE;
        }
        if (!isDisabled(entity)) {
            if (profile != null) {
                profile.diet = resolved;
            } else {
                cfg.animalProfiles.putIfAbsent(id, new AnimalProfile(resolved));
            }
            ConfigManager.scheduleSave();
        }
        return resolved;
    }

    public static WeightStats statsOf(Entity entity) {
        return ConfigManager.statsFor(idOf(entity));
    }

    public static boolean isSleeping(Level level) {
        if (!ConfigManager.get().pauseAtNight) {
            return false;
        }
        if (level.dimensionType().hasFixedTime()) {
            return false;
        }
        return level.getSkyDarken() >= 4;
    }

    private static String idOf(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}

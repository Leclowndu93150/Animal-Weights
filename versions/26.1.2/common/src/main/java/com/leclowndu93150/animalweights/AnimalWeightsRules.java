package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import com.leclowndu93150.animalweights.config.EntityFilterMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class AnimalWeightsRules {
    private AnimalWeightsRules() {
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
            case BLACKLIST -> cfg.disabledEntities != null && cfg.disabledEntities.contains(id);
        };
    }

    public static Diet dietOf(Entity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        String id = idOf(entity);
        Diet d = cfg.entityDiets == null ? null : cfg.entityDiets.get(id);
        if (d != null) {
            return d;
        }
        Diet resolved = DietResolver.resolveByTag(entity.getType());
        if (resolved == null) {
            resolved = DietResolver.resolveBySpawnBiomes(entity);
        }
        if (resolved == null) {
            resolved = cfg.defaultDiet != null ? cfg.defaultDiet : Diet.OMNIVORE;
        }
        if (cfg.entityDiets != null && !isDisabled(entity)) {
            cfg.entityDiets.put(id, resolved);
            ConfigManager.scheduleSave();
        }
        return resolved;
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

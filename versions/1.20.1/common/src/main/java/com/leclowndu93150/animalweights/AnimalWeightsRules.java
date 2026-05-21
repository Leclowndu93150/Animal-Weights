package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class AnimalWeightsRules {
    private AnimalWeightsRules() {
    }

    public static boolean isDisabled(Entity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        if (cfg.disabledEntities == null || cfg.disabledEntities.isEmpty()) {
            return false;
        }
        return cfg.disabledEntities.contains(idOf(entity));
    }

    public static Diet dietOf(Entity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        Diet d = cfg.entityDiets == null ? null : cfg.entityDiets.get(idOf(entity));
        if (d != null) {
            return d;
        }
        return cfg.defaultDiet != null ? cfg.defaultDiet : Diet.OMNIVORE;
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

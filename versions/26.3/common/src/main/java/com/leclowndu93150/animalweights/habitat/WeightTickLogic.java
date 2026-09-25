package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import com.leclowndu93150.animalweights.config.WeightStats;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public final class WeightTickLogic {
    private WeightTickLogic() {
    }

    public static void tick(Animal animal, ServerLevel level) {
        if (animal.isBaby()) {
            return;
        }
        if (AnimalWeightsRules.isDisabled(animal)) {
            return;
        }
        if (!AnimalWeightsRules.isActive(animal)) {
            return;
        }
        if (AnimalWeightsRules.isSleeping(level)) {
            return;
        }
        Diet diet = AnimalWeightsRules.dietOf(animal);
        if (isOutOfElement(level, diet)) {
            return;
        }
        AnimalWeightsConfig cfg = ConfigManager.get();
        WeightStats stats = AnimalWeightsRules.statsOf(animal);
        WeightData data = WeightAttachment.get(animal);
        data.incrementTicksSinceEvaluation();
        if (data.getTicksSinceEvaluation() < stats.weightTickIntervalTicks()) {
            return;
        }
        data.resetTicksSinceEvaluation();

        BlockPos pos = animal.blockPosition();
        boolean naturalWater = HabitatScanner.hasUsableNaturalWater(level, pos, diet);
        BlockPos cauldronPos = naturalWater ? null : HabitatScanner.findUsableCauldron(level, pos, diet);
        boolean water = naturalWater || cauldronPos != null;

        boolean grazing = false;
        BlockPos troughPos = null;
        if (diet == Diet.HERBIVORE || (diet == Diet.OMNIVORE && !water)) {
            grazing = HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius);
            if (!grazing) {
                troughPos = HabitatScanner.findUsableFeedingTrough(animal, level, pos, diet);
                grazing = troughPos != null;
            }
        }

        int score = scoreHabitat(animal, level, cfg, diet, water, grazing);
        int requiredForGain = requiredScoreForGain(diet);

        int weight = WeightAttachment.getWeight(animal);
        boolean resistant = cfg.naturalSpawnSicknessResistance && data.isNaturallySpawned();
        if (score >= requiredForGain) {
            if (animal.getRandom().nextFloat() < stats.weightGainChance()) {
                weight = stats.clamp(weight + 1);
                if (cauldronPos != null) {
                    HabitatScanner.drainWaterCauldron(level, cauldronPos);
                }
                if (troughPos != null) {
                    HabitatScanner.eatFromFeedingTrough(animal, level, troughPos);
                }
            }
        } else if (score == requiredForGain - 2) {
            double chance = resistant ? stats.weightMinorLossChance() * 0.5 : stats.weightMinorLossChance();
            if (animal.getRandom().nextFloat() < chance) {
                weight = stats.clamp(weight - 1);
            }
        } else if (score < requiredForGain - 2) {
            double chance = resistant ? stats.weightSevereLossChance() * 0.5 : stats.weightSevereLossChance();
            if (animal.getRandom().nextFloat() < chance) {
                weight = stats.clamp(weight - 1);
            }
        }
        WeightAttachment.setWeight(animal, weight);
    }

    public static int scoreHabitat(Animal animal, Level level, AnimalWeightsConfig cfg, Diet diet, boolean water, boolean grazing) {
        BlockPos pos = animal.blockPosition();
        boolean light = HabitatScanner.hasBrightLight(level, pos);
        boolean space = hasSpace(animal, level, cfg);
        int score = 0;
        if (light) score++;
        if (space) score++;
        switch (diet) {
            case HERBIVORE -> {
                if (water) score++;
                if (grazing) score++;
            }
            case CARNIVORE -> {
                if (water) score++;
                score++;
            }
            case AQUATIC -> {
                if (water) score += 2;
            }
            case OMNIVORE -> {
                if (water || grazing) score++;
                score++;
            }
            case NETHER -> {
                score++;
                if (HabitatScanner.hasLavaNearby(level, pos, cfg.habitatScanRadius)) score++;
                if (HabitatScanner.hasNetherGroundNearby(level, pos, cfg.habitatScanRadius)) score++;
            }
        }
        return score;
    }

    public static boolean hasSpace(Animal animal, Level level, AnimalWeightsConfig cfg) {
        if (HabitatScanner.isCrowded(animal)) {
            return false;
        }
        if (cfg.requireOpenSpace) {
            return HabitatScanner.hasOpenSpace(level, animal.blockPosition(), cfg.habitatScanRadius, cfg.minOpenSpace);
        }
        return true;
    }

    public static boolean isOutOfElement(Level level, Diet diet) {
        boolean inNether = level.dimension() == Level.NETHER;
        if (diet == Diet.NETHER) {
            if (ConfigManager.get().netherAnimalsGainAnywhere) {
                return false;
            }
            return !inNether;
        }
        return inNether;
    }

    public static int requiredScoreForGain(Diet diet) {
        return 4;
    }
}

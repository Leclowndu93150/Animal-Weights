package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
        if (AnimalWeightsRules.isSleeping(level)) {
            return;
        }
        Diet diet = AnimalWeightsRules.dietOf(animal);
        if (isOutOfElement(level, diet)) {
            return;
        }
        AnimalWeightsConfig cfg = ConfigManager.get();
        WeightData data = WeightAttachment.get(animal);
        data.incrementTicksSinceEvaluation();
        if (data.getTicksSinceEvaluation() < cfg.weightTickIntervalTicks) {
            return;
        }
        data.resetTicksSinceEvaluation();

        BlockPos pos = animal.blockPosition();
        boolean naturalWater = diet != Diet.NETHER && HabitatScanner.hasWaterNearby(level, pos, cfg.habitatScanRadius);
        BlockPos cauldronPos = null;
        if (!naturalWater && diet != Diet.NETHER && cfg.cauldronCountsAsWater) {
            cauldronPos = HabitatScanner.findFullWaterCauldronNearby(level, pos, cfg.cauldronScanRadius);
        }
        boolean water = naturalWater || cauldronPos != null;

        int score = scoreHabitat(animal, level, cfg, diet, water);
        int requiredForGain = requiredScoreForGain(diet);

        int weight = WeightAttachment.getWeight(animal);
        boolean resistant = cfg.naturalSpawnSicknessResistance && data.isNaturallySpawned();
        if (score >= requiredForGain) {
            if (animal.getRandom().nextFloat() < cfg.weightGainChance) {
                weight = Mth.clamp(weight + 1, cfg.minWeight, cfg.maxWeight);
                if (cauldronPos != null) {
                    HabitatScanner.drainWaterCauldron(level, cauldronPos);
                }
            }
        } else if (score == requiredForGain - 1) {
            // stable
        } else if (score == requiredForGain - 2) {
            double chance = resistant ? cfg.weightMinorLossChance * 0.5 : cfg.weightMinorLossChance;
            if (animal.getRandom().nextFloat() < chance) {
                weight = Mth.clamp(weight - 1, cfg.minWeight, cfg.maxWeight);
            }
        } else {
            double chance = resistant ? cfg.weightSevereLossChance * 0.5 : cfg.weightSevereLossChance;
            if (animal.getRandom().nextFloat() < chance) {
                weight = Mth.clamp(weight - 1, cfg.minWeight, cfg.maxWeight);
            }
        }
        WeightAttachment.setWeight(animal, weight);
    }

    public static int scoreHabitat(Animal animal, Level level, AnimalWeightsConfig cfg, Diet diet, boolean water) {
        BlockPos pos = animal.blockPosition();
        boolean light = HabitatScanner.hasBrightLight(level, pos);
        boolean notCrowded = !HabitatScanner.isCrowded(animal);
        int score = 0;
        if (light) score++;
        if (notCrowded) score++;
        switch (diet) {
            case HERBIVORE -> {
                if (water) score++;
                if (HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius)) score++;
            }
            case CARNIVORE -> {
                if (water) score++;
                score++;
            }
            case AQUATIC -> {
                if (water) score += 2;
            }
            case OMNIVORE -> {
                if (water || HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius)) score++;
                score++;
            }
            case NETHER -> {
                if (HabitatScanner.hasLavaNearby(level, pos, cfg.habitatScanRadius)) score++;
                if (HabitatScanner.hasNetherGroundNearby(level, pos, cfg.habitatScanRadius)) score++;
            }
        }
        return score;
    }

    public static boolean isOutOfElement(Level level, Diet diet) {
        boolean inNether = level.dimension() == Level.NETHER;
        if (diet == Diet.NETHER) {
            return !inNether;
        }
        return inNether;
    }

    public static int requiredScoreForGain(Diet diet) {
        return 4;
    }
}

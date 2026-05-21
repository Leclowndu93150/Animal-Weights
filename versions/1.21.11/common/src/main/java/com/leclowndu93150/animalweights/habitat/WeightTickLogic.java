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
        AnimalWeightsConfig cfg = ConfigManager.get();
        WeightData data = WeightAttachment.get(animal);
        data.incrementTicksSinceEvaluation();
        if (data.getTicksSinceEvaluation() < cfg.weightTickIntervalTicks) {
            return;
        }
        data.resetTicksSinceEvaluation();

        int score = scoreHabitat(animal, level, cfg);
        int requiredForGain = requiredScoreForGain(AnimalWeightsRules.dietOf(animal));

        int weight = WeightAttachment.getWeight(animal);
        if (score >= requiredForGain) {
            if (animal.getRandom().nextFloat() < cfg.weightGainChance) {
                weight = Mth.clamp(weight + 1, cfg.minWeight, cfg.maxWeight);
            }
        } else if (score == requiredForGain - 1) {
            // stable
        } else if (score == requiredForGain - 2) {
            if (animal.getRandom().nextFloat() < cfg.weightMinorLossChance) {
                weight = Mth.clamp(weight - 1, cfg.minWeight, cfg.maxWeight);
            }
        } else {
            if (animal.getRandom().nextFloat() < cfg.weightSevereLossChance) {
                weight = Mth.clamp(weight - 1, cfg.minWeight, cfg.maxWeight);
            }
        }
        WeightAttachment.setWeight(animal, weight);
    }

    public static int scoreHabitat(Animal animal, net.minecraft.world.level.Level level, AnimalWeightsConfig cfg) {
        BlockPos pos = animal.blockPosition();
        Diet diet = AnimalWeightsRules.dietOf(animal);
        boolean light = HabitatScanner.hasBrightLight(level, pos);
        boolean water = HabitatScanner.hasWaterNearby(level, pos, cfg.habitatScanRadius);
        boolean grazing = HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius);
        boolean notCrowded = !HabitatScanner.isCrowded(animal);
        int score = 0;
        if (light) score++;
        if (notCrowded) score++;
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
        }
        return score;
    }

    public static int requiredScoreForGain(Diet diet) {
        return 4;
    }
}

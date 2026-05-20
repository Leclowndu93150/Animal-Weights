package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
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
        AnimalWeightsConfig cfg = ConfigManager.get();
        WeightData data = WeightAttachment.get(animal);
        data.incrementTicksSinceEvaluation();
        if (data.getTicksSinceEvaluation() < cfg.weightTickIntervalTicks) {
            return;
        }
        data.resetTicksSinceEvaluation();

        BlockPos pos = animal.blockPosition();
        int score = 0;
        if (HabitatScanner.hasBrightLight(level, pos)) score++;
        if (HabitatScanner.hasWaterNearby(level, pos, cfg.habitatScanRadius)) score++;
        if (HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius)) score++;
        if (!HabitatScanner.isCrowded(animal)) score++;

        int weight = WeightAttachment.getWeight(animal);
        if (score >= 4) {
            if (animal.getRandom().nextFloat() < cfg.weightGainChance) {
                weight = Mth.clamp(weight + 1, cfg.minWeight, cfg.maxWeight);
            }
        } else if (score == 3) {
            // stable
        } else if (score == 2) {
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
}

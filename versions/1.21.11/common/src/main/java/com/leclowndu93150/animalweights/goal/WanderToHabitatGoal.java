package com.leclowndu93150.animalweights.goal;

import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WanderToHabitatGoal extends RandomStrollGoal {
    private static final int SAMPLES = 8;

    public WanderToHabitatGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 200);
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        Vec3 best = null;
        int bestScore = -1;
        for (int i = 0; i < SAMPLES; i++) {
            Vec3 candidate = LandRandomPos.getPos(this.mob, 10, 7);
            if (candidate == null) continue;
            int score = scorePosition(BlockPos.containing(candidate));
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private int scorePosition(BlockPos pos) {
        int radius = ConfigManager.get().habitatScanRadius;
        int score = 0;
        if (HabitatScanner.hasBrightLight(this.mob.level(), pos)) score++;
        if (HabitatScanner.hasWaterNearby(this.mob.level(), pos, radius)) score++;
        if (HabitatScanner.hasGrazingNearby(this.mob.level(), pos, radius)) score++;
        return score;
    }
}

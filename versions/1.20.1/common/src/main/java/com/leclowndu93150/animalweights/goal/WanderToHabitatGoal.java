package com.leclowndu93150.animalweights.goal;

import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WanderToHabitatGoal extends RandomStrollGoal {
    private static final int SAMPLES = 4;

    public WanderToHabitatGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 200);
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        Vec3 best = null;
        int bestScore = -1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < SAMPLES; i++) {
            Vec3 candidate = LandRandomPos.getPos(this.mob, 10, 7);
            if (candidate == null) continue;
            pos.set(candidate.x, candidate.y, candidate.z);
            int score = HabitatScanner.quickHabitatScore(this.mob.level(), pos);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
                if (score == 3) break;
            }
        }
        return best;
    }
}

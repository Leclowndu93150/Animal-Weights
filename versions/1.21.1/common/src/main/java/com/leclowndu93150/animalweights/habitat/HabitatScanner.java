package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class HabitatScanner {
    private HabitatScanner() {
    }

    public static boolean hasBrightLight(Level level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos) >= ConfigManager.get().lightThreshold;
    }

    public static boolean hasWaterNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasGrazingNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCrowded(LivingEntity entity) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        int r = cfg.crowdRadius;
        int limit = cfg.crowdLimit;
        AABB box = new AABB(
            entity.getX() - r, entity.getY() - r, entity.getZ() - r,
            entity.getX() + r, entity.getY() + r, entity.getZ() + r
        );
        EntityType<?> type = entity.getType();
        List<LivingEntity> nearby = entity.level().getEntitiesOfClass(
            LivingEntity.class, box, other -> other != entity && other.isAlive() && other.getType() == type
        );
        return nearby.size() > limit;
    }

    public static int quickHabitatScore(Level level, BlockPos pos) {
        int score = 0;
        if (level.getMaxLocalRawBrightness(pos) >= ConfigManager.get().lightThreshold) score++;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        boolean foundWater = false;
        boolean foundGrazing = false;
        for (int dx = -1; dx <= 1 && !(foundWater && foundGrazing); dx++) {
            for (int dz = -1; dz <= 1 && !(foundWater && foundGrazing); dz++) {
                if (!foundWater) {
                    cursor.set(cx + dx, cy, cz + dz);
                    if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                        foundWater = true;
                    }
                }
                if (!foundGrazing) {
                    cursor.set(cx + dx, cy - 1, cz + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)) {
                        foundGrazing = true;
                    }
                }
            }
        }
        if (foundWater) score++;
        if (foundGrazing) score++;
        return score;
    }

    public static boolean isNearWaterOrVillage(ServerLevel level, BlockPos pos) {
        if (level.isVillage(pos)) {
            return true;
        }
        return hasWaterNearby(level, pos, ConfigManager.get().proximityRadius);
    }

    public static boolean isNearWaterOrVillageCached(Animal entity, ServerLevel level) {
        if (!ConfigManager.get().enableProximityBonus) {
            return false;
        }
        WeightData data = WeightAttachment.get(entity);
        long tick = level.getGameTime();
        if (data.getBonusCacheTick() == tick) {
            return data.getBonusCacheValue();
        }
        boolean value = isNearWaterOrVillage(level, entity.blockPosition());
        data.storeBonusCache(tick, value);
        return value;
    }
}

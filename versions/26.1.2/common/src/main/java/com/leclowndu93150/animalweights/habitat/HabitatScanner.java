package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class HabitatScanner {
    private HabitatScanner() {
    }

    public static boolean hasBrightLight(Level level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) >= ConfigManager.get().lightThreshold;
    }

    public static boolean hasWaterNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
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
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getBlockState(cursor).is(Blocks.GRASS_BLOCK) || level.getBlockState(cursor).is(Blocks.MOSS_BLOCK)) {
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
        AABB box = new AABB(
            entity.getX() - r, entity.getY() - 1, entity.getZ() - r,
            entity.getX() + r, entity.getY() + 2, entity.getZ() + r
        );
        List<? extends LivingEntity> sameSpecies = entity.level().getEntitiesOfClass(
            entity.getClass(), box, other -> other != entity && other.isAlive()
        );
        return sameSpecies.size() > cfg.crowdLimit;
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

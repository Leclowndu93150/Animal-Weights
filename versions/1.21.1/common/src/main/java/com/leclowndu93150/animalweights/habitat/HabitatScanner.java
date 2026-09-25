package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class HabitatScanner {
    private static final String FEEDING_TROUGH_ID = "animal_feeding_trough:feeding_trough";

    private HabitatScanner() {
    }

    public static boolean hasBrightLight(Level level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos) >= ConfigManager.get().lightThreshold;
    }

    public static boolean acceptsNaturalWater(Diet diet) {
        return diet != Diet.NETHER && ConfigManager.get().waterSource.acceptsNatural(diet);
    }

    public static boolean acceptsCauldronWater(Diet diet) {
        return diet != Diet.NETHER && ConfigManager.get().waterSource.acceptsCauldron();
    }

    public static boolean hasUsableNaturalWater(Level level, BlockPos pos, Diet diet) {
        return acceptsNaturalWater(diet) && hasWaterNearby(level, pos, ConfigManager.get().habitatScanRadius);
    }

    public static BlockPos findUsableCauldron(Level level, BlockPos pos, Diet diet) {
        if (!acceptsCauldronWater(diet)) {
            return null;
        }
        return findFullWaterCauldronNearby(level, pos, ConfigManager.get().feederScanRadius);
    }

    public static boolean hasUsableFeedingTrough(Animal animal) {
        return findUsableFeedingTrough(animal, animal.level(), animal.blockPosition(), AnimalWeightsRules.dietOf(animal)) != null;
    }

    public static BlockPos findUsableFeedingTrough(Animal animal, Level level, BlockPos pos, Diet diet) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        if (!cfg.feedingTroughCountsAsGrazing || (diet != Diet.HERBIVORE && diet != Diet.OMNIVORE)) {
            return null;
        }
        return findFeedingTroughNearby(animal, level, pos, cfg.feederScanRadius);
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
                    if (GrazingBlocks.matches(level.getBlockState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasLavaNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    if (level.getFluidState(cursor).is(FluidTags.LAVA)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasNetherGroundNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.NETHERRACK)) {
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

    public static boolean hasOpenSpace(Level level, BlockPos center, int radius, int required) {
        if (required <= 0) {
            return true;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int open = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    if (level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                        open++;
                        if (open >= required) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int quickHabitatScoreFor(Diet diet, Level level, BlockPos pos) {
        if (diet != Diet.NETHER) {
            return quickHabitatScore(diet, level, pos);
        }
        int score = 0;
        if (level.getMaxLocalRawBrightness(pos) >= ConfigManager.get().lightThreshold) score++;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        boolean foundLava = false;
        boolean foundGround = false;
        for (int dx = -1; dx <= 1 && !(foundLava && foundGround); dx++) {
            for (int dz = -1; dz <= 1 && !(foundLava && foundGround); dz++) {
                if (!foundLava) {
                    cursor.set(cx + dx, cy, cz + dz);
                    if (level.getFluidState(cursor).is(FluidTags.LAVA)) {
                        foundLava = true;
                    }
                }
                if (!foundGround) {
                    cursor.set(cx + dx, cy - 1, cz + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.NETHERRACK)) {
                        foundGround = true;
                    }
                }
            }
        }
        if (foundLava) score++;
        if (foundGround) score++;
        return score;
    }

    private static int quickHabitatScore(Diet diet, Level level, BlockPos pos) {
        int score = 0;
        if (level.getMaxLocalRawBrightness(pos) >= ConfigManager.get().lightThreshold) score++;
        boolean naturalWater = acceptsNaturalWater(diet);
        boolean cauldronWater = acceptsCauldronWater(diet);
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
                    if (naturalWater && level.getFluidState(cursor).is(FluidTags.WATER)
                        || cauldronWater && isFilledWaterCauldron(level.getBlockState(cursor))) {
                        foundWater = true;
                    }
                }
                if (!foundGrazing) {
                    cursor.set(cx + dx, cy - 1, cz + dz);
                    if (GrazingBlocks.matches(level.getBlockState(cursor))) {
                        foundGrazing = true;
                    }
                }
            }
        }
        if (foundWater) score++;
        if (foundGrazing) score++;
        return score;
    }

    public static BlockPos findFullWaterCauldronNearby(Level level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(cx + dx, cy + dy, cz + dz);
                    if (isFilledWaterCauldron(level.getBlockState(cursor))) {
                        return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }

    public static void drainWaterCauldron(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isFilledWaterCauldron(state)) {
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
    }

    private static boolean isFilledWaterCauldron(BlockState state) {
        return state.is(Blocks.WATER_CAULDRON) && state.getValue(BlockStateProperties.LEVEL_CAULDRON) >= 1;
    }

    public static BlockPos findFeedingTroughNearby(Animal animal, Level level, BlockPos center, int radius) {
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + radius);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos troughPos = blockEntity.getBlockPos();
                    if (Math.abs(troughPos.getX() - center.getX()) > radius
                        || Math.abs(troughPos.getY() - center.getY()) > radius
                        || Math.abs(troughPos.getZ() - center.getZ()) > radius) {
                        continue;
                    }
                    if (isFeedingTroughWithFood(animal, blockEntity)) {
                        return troughPos;
                    }
                }
            }
        }
        return null;
    }

    public static void eatFromFeedingTrough(Animal animal, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof Container trough && animal.isFood(trough.getItem(0))) {
            trough.removeItem(0, 1);
        }
    }

    private static boolean isFeedingTroughWithFood(Animal animal, BlockEntity blockEntity) {
        if (!(blockEntity instanceof Container trough)
            || !FEEDING_TROUGH_ID.equals(String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType())))) {
            return false;
        }
        ItemStack food = trough.getItem(0);
        return !food.isEmpty() && animal.isFood(food);
    }

    public static boolean isNearWaterOrVillage(ServerLevel level, BlockPos pos, Diet diet) {
        if (level.isVillage(pos)) {
            return true;
        }
        AnimalWeightsConfig cfg = ConfigManager.get();
        if (cfg.waterSource.acceptsNatural(diet)) {
            return hasWaterNearby(level, pos, cfg.proximityRadius);
        }
        return findFullWaterCauldronNearby(level, pos, cfg.proximityRadius) != null;
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
        boolean value = isNearWaterOrVillage(level, entity.blockPosition(), AnimalWeightsRules.dietOf(entity));
        data.storeBonusCache(tick, value);
        return value;
    }
}

package com.leclowndu93150.animalweights.habitat;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GrazingBlocks {
    private static int cachedGeneration = -1;
    private static Set<Block> extraBlocks = Set.of();
    private static List<TagKey<Block>> extraTags = List.of();

    private GrazingBlocks() {
    }

    public static boolean matches(BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)
            || state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)) {
            return true;
        }
        refresh();
        if (!extraBlocks.isEmpty() && extraBlocks.contains(state.getBlock())) {
            return true;
        }
        for (TagKey<Block> tag : extraTags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static void refresh() {
        int generation = ConfigManager.generation();
        if (generation == cachedGeneration) {
            return;
        }
        cachedGeneration = generation;
        AnimalWeightsConfig cfg = ConfigManager.get();
        Set<Block> blocks = new HashSet<>();
        for (String id : cfg.grazingBlocks) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                BuiltInRegistries.BLOCK.getOptional(key).ifPresent(blocks::add);
            }
        }
        List<TagKey<Block>> tags = new ArrayList<>();
        for (String id : cfg.grazingBlockTags) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                tags.add(TagKey.create(Registries.BLOCK, key));
            }
        }
        extraBlocks = blocks;
        extraTags = tags;
    }
}

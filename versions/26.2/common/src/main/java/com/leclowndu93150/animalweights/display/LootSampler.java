package com.leclowndu93150.animalweights.display;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class LootSampler {
    public static final int MAX_DISPLAY_ITEMS = 3;
    private static final int SAMPLES = 12;

    private LootSampler() {
    }

    public static List<Item> sample(LivingEntity entity, ServerLevel level) {
        Optional<ResourceKey<LootTable>> keyOpt = entity.getLootTable();
        if (keyOpt.isEmpty()) {
            return List.of();
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(keyOpt.get());
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
            .create(LootContextParamSets.ENTITY);
        Set<Item> unique = new LinkedHashSet<>();
        for (int i = 0; i < SAMPLES && unique.size() < MAX_DISPLAY_ITEMS; i++) {
            table.getRandomItems(params, entity.getRandom().nextLong(), stack -> {
                if (!stack.isEmpty()) {
                    unique.add(stack.getItem());
                }
            });
        }
        if (unique.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(unique).subList(0, Math.min(unique.size(), MAX_DISPLAY_ITEMS));
    }
}

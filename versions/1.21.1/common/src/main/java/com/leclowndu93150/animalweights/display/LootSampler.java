package com.leclowndu93150.animalweights.display;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LootSampler {
    public static final int MAX_DISPLAY_ITEMS = 3;
    private static final int SAMPLES = 12;

    private LootSampler() {
    }

    public static List<Item> sample(LivingEntity entity, ServerLevel level) {
        ResourceKey<LootTable> key = entity.getLootTable();
        if (key == null) {
            return List.of();
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
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

    public static String encode(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(BuiltInRegistries.ITEM.getKey(items.get(i)));
        }
        return sb.toString();
    }

    public static List<ItemStack> decode(String csv) {
        if (csv == null || csv.isEmpty()) return List.of();
        List<ItemStack> out = new ArrayList<>();
        for (String token : csv.split(",")) {
            ResourceLocation id = ResourceLocation.tryParse(token);
            if (id == null) continue;
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> out.add(new ItemStack(item)));
        }
        return out;
    }
}

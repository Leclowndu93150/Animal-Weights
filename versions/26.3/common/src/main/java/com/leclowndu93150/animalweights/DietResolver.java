package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.Diet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.Weighted;

import java.util.EnumSet;
import java.util.Set;

public final class DietResolver {
    public static final TagKey<EntityType<?>> HERBIVORE = tag("herbivore");
    public static final TagKey<EntityType<?>> CARNIVORE = tag("carnivore");
    public static final TagKey<EntityType<?>> OMNIVORE = tag("omnivore");
    public static final TagKey<EntityType<?>> AQUATIC = tag("aquatic");
    public static final TagKey<EntityType<?>> NETHER = tag("nether");
    public static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("animalweights", "livestock"));

    private static final Set<MobCategory> AQUATIC_CATEGORIES = EnumSet.of(
        MobCategory.WATER_CREATURE,
        MobCategory.WATER_AMBIENT,
        MobCategory.UNDERGROUND_WATER_CREATURE,
        MobCategory.AXOLOTLS
    );

    private DietResolver() {
    }

    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("animalweights", "diet/" + name));
    }

    public static Diet resolveByTag(EntityType<?> type) {
        if (type.builtInRegistryHolder().is(NETHER)) return Diet.NETHER;
        if (type.builtInRegistryHolder().is(AQUATIC)) return Diet.AQUATIC;
        if (type.builtInRegistryHolder().is(CARNIVORE)) return Diet.CARNIVORE;
        if (type.builtInRegistryHolder().is(HERBIVORE)) return Diet.HERBIVORE;
        if (type.builtInRegistryHolder().is(OMNIVORE)) return Diet.OMNIVORE;
        return null;
    }

    public static boolean isLivestock(EntityType<?> type) {
        return type.builtInRegistryHolder().is(LIVESTOCK);
    }

    public static Diet resolveBySpawnBiomes(Entity entity) {
        Level level = entity.level();
        if (level == null) return null;
        Registry<Biome> biomes;
        try {
            biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        } catch (Exception e) {
            return null;
        }
        EntityType<?> type = entity.getType();
        boolean foundAquatic = false;
        boolean foundMonsterOnly = false;
        boolean foundCreature = false;
        boolean foundInCold = false;
        boolean foundAny = false;
        for (Holder.Reference<Biome> ref : biomes.listElements().toList()) {
            Biome biome = ref.value();
            MobSpawnSettings settings = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
            boolean inThisBiome = false;
            boolean inCreatureHere = false;
            boolean inMonsterHere = false;
            for (MobCategory cat : MobCategory.values()) {
                for (Weighted<MobSpawnSettings.SpawnerData> entry : settings.getMobsToSpawn(cat).unwrap()) {
                    if (entry.value().type() != type) continue;
                    inThisBiome = true;
                    if (AQUATIC_CATEGORIES.contains(cat)) foundAquatic = true;
                    if (cat == MobCategory.CREATURE) inCreatureHere = true;
                    if (cat == MobCategory.MONSTER) inMonsterHere = true;
                }
            }
            if (inThisBiome) {
                foundAny = true;
                if (inCreatureHere) foundCreature = true;
                if (inMonsterHere && !inCreatureHere) foundMonsterOnly = true;
                if (biome.getBaseTemperature() < 0.2F || ref.is(BiomeTags.IS_OCEAN)) foundInCold = true;
            }
        }
        if (!foundAny) return null;
        if (foundAquatic) return Diet.AQUATIC;
        if (foundMonsterOnly && !foundCreature) return Diet.CARNIVORE;
        if (foundInCold && !foundCreature) return Diet.CARNIVORE;
        if (foundCreature) return Diet.HERBIVORE;
        return null;
    }
}

package com.leclowndu93150.animalweights.config;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AnimalWeightsConfig {
    public ScalingMode dropScalingMode = ScalingMode.MULTIPLICATIVE;
    public ScalingMode xpScalingMode = ScalingMode.MULTIPLICATIVE;

    public int minWeight = 0;
    public int defaultWeight = 1;
    public int maxWeight = 8;
    public int sickThreshold = 0;

    public int weightTickIntervalTicks = 8000;
    public double weightGainChance = 0.5;
    public double weightMinorLossChance = 0.25;
    public double weightSevereLossChance = 0.5;

    public int lightThreshold = 10;
    public int habitatScanRadius = 4;
    public int crowdRadius = 2;
    public int crowdLimit = 6;

    public boolean enableProximityBonus = true;
    public int proximityRadius = 6;

    public boolean enableSickTint = true;
    public boolean enableSickParticles = true;
    public boolean enableOverlay = true;
    public int sickTintColor = 0xFF80C070;

    public OverlayMode overlayMode = OverlayMode.LOOKING;
    public double overlayRange = 8.0;

    public boolean pauseAtNight = true;

    public Diet defaultDiet = Diet.OMNIVORE;
    public Map<String, Diet> entityDiets = defaultEntityDiets();
    public Set<String> disabledEntities = new LinkedHashSet<>();

    public AnimalWeightsConfig() {
    }

    private static Map<String, Diet> defaultEntityDiets() {
        Map<String, Diet> map = new HashMap<>();
        map.put("minecraft:cow", Diet.HERBIVORE);
        map.put("minecraft:mooshroom", Diet.HERBIVORE);
        map.put("minecraft:sheep", Diet.HERBIVORE);
        map.put("minecraft:pig", Diet.HERBIVORE);
        map.put("minecraft:horse", Diet.HERBIVORE);
        map.put("minecraft:donkey", Diet.HERBIVORE);
        map.put("minecraft:mule", Diet.HERBIVORE);
        map.put("minecraft:llama", Diet.HERBIVORE);
        map.put("minecraft:trader_llama", Diet.HERBIVORE);
        map.put("minecraft:rabbit", Diet.HERBIVORE);
        map.put("minecraft:goat", Diet.HERBIVORE);
        map.put("minecraft:camel", Diet.HERBIVORE);
        map.put("minecraft:happy_ghast", Diet.HERBIVORE);
        map.put("minecraft:chicken", Diet.OMNIVORE);
        map.put("minecraft:panda", Diet.OMNIVORE);
        map.put("minecraft:fox", Diet.OMNIVORE);
        map.put("minecraft:turtle", Diet.OMNIVORE);
        map.put("minecraft:armadillo", Diet.OMNIVORE);
        map.put("minecraft:sniffer", Diet.OMNIVORE);
        map.put("minecraft:bee", Diet.OMNIVORE);
        map.put("minecraft:hoglin", Diet.OMNIVORE);
        map.put("minecraft:wolf", Diet.CARNIVORE);
        map.put("minecraft:cat", Diet.CARNIVORE);
        map.put("minecraft:ocelot", Diet.CARNIVORE);
        map.put("minecraft:polar_bear", Diet.CARNIVORE);
        map.put("minecraft:frog", Diet.CARNIVORE);
        map.put("minecraft:axolotl", Diet.AQUATIC);
        map.put("minecraft:dolphin", Diet.AQUATIC);
        map.put("minecraft:squid", Diet.AQUATIC);
        map.put("minecraft:glow_squid", Diet.AQUATIC);
        map.put("minecraft:cod", Diet.AQUATIC);
        map.put("minecraft:salmon", Diet.AQUATIC);
        map.put("minecraft:tropical_fish", Diet.AQUATIC);
        map.put("minecraft:pufferfish", Diet.AQUATIC);
        map.put("minecraft:tadpole", Diet.AQUATIC);
        return map;
    }
}

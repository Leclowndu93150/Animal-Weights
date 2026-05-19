package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public final class WeightData {
    public static int minWeight() {
        return ConfigManager.get().minWeight;
    }

    public static int defaultWeight() {
        return ConfigManager.get().defaultWeight;
    }

    public static int maxWeight() {
        return ConfigManager.get().maxWeight;
    }

    private int weight;
    private long bonusCacheTick = Long.MIN_VALUE;
    private boolean bonusCacheValue;

    public WeightData() {
        this(defaultWeight());
    }

    public WeightData(int weight) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
    }

    public long getBonusCacheTick() {
        return bonusCacheTick;
    }

    public boolean getBonusCacheValue() {
        return bonusCacheValue;
    }

    public void storeBonusCache(long tick, boolean value) {
        this.bonusCacheTick = tick;
        this.bonusCacheValue = value;
    }

    public void save(CompoundTag tag) {
        tag.putInt("weight", this.weight);
    }

    public static WeightData load(CompoundTag tag) {
        int w = tag.contains("weight") ? tag.getInt("weight") : defaultWeight();
        return new WeightData(w);
    }
}

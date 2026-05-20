package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    private int ticksSinceEvaluation;
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

    public int getTicksSinceEvaluation() {
        return ticksSinceEvaluation;
    }

    public void incrementTicksSinceEvaluation() {
        this.ticksSinceEvaluation++;
    }

    public void resetTicksSinceEvaluation() {
        this.ticksSinceEvaluation = 0;
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

    public void save(ValueOutput output) {
        output.putInt("weight", this.weight);
        output.putInt("ticksSinceEvaluation", this.ticksSinceEvaluation);
    }

    public static WeightData load(ValueInput input) {
        int w = input.getIntOr("weight", defaultWeight());
        WeightData data = new WeightData(w);
        data.ticksSinceEvaluation = Math.max(0, input.getIntOr("ticksSinceEvaluation", 0));
        return data;
    }
}

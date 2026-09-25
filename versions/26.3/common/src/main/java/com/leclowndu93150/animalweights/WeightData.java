package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.WeightStats;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WeightData {
    private int weight;
    private int ticksSinceEvaluation;
    private long bonusCacheTick = Long.MIN_VALUE;
    private boolean bonusCacheValue;
    private boolean naturallySpawned;
    private boolean engaged;

    public WeightData(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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

    public boolean isNaturallySpawned() {
        return naturallySpawned;
    }

    public void setNaturallySpawned(boolean naturallySpawned) {
        this.naturallySpawned = naturallySpawned;
    }

    public boolean isEngaged() {
        return engaged;
    }

    public void setEngaged(boolean engaged) {
        this.engaged = engaged;
    }

    public void save(ValueOutput output) {
        output.putInt("weight", this.weight);
        output.putInt("ticksSinceEvaluation", this.ticksSinceEvaluation);
        output.putBoolean("naturallySpawned", this.naturallySpawned);
        output.putBoolean("engaged", this.engaged);
    }

    public static WeightData load(ValueInput input, WeightStats stats) {
        WeightData data = new WeightData(stats.clamp(input.getIntOr("weight", stats.defaultWeight())));
        data.ticksSinceEvaluation = Math.max(0, input.getIntOr("ticksSinceEvaluation", 0));
        data.naturallySpawned = input.getBooleanOr("naturallySpawned", false);
        data.engaged = input.getBooleanOr("engaged", false);
        return data;
    }
}

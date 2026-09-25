package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.WeightStats;
import net.minecraft.nbt.CompoundTag;

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

    public void save(CompoundTag tag) {
        tag.putInt("weight", this.weight);
        tag.putInt("ticksSinceEvaluation", this.ticksSinceEvaluation);
        tag.putBoolean("naturallySpawned", this.naturallySpawned);
        tag.putBoolean("engaged", this.engaged);
    }

    public static WeightData load(CompoundTag tag, WeightStats stats) {
        WeightData data = new WeightData(stats.clamp(tag.contains("weight") ? tag.getInt("weight") : stats.defaultWeight()));
        if (tag.contains("ticksSinceEvaluation")) {
            data.ticksSinceEvaluation = Math.max(0, tag.getInt("ticksSinceEvaluation"));
        }
        if (tag.contains("naturallySpawned")) {
            data.naturallySpawned = tag.getBoolean("naturallySpawned");
        }
        if (tag.contains("engaged")) {
            data.engaged = tag.getBoolean("engaged");
        }
        return data;
    }
}

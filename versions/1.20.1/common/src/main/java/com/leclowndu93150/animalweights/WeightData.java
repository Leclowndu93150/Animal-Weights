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
    private String lootPreview;
    private long bonusCacheTick = Long.MIN_VALUE;
    private boolean bonusCacheValue;

    public WeightData() {
        this(defaultWeight(), "");
    }

    public WeightData(int weight, String lootPreview) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
        this.lootPreview = lootPreview == null ? "" : lootPreview;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
    }

    public String getLootPreview() {
        return lootPreview;
    }

    public void setLootPreview(String lootPreview) {
        this.lootPreview = lootPreview == null ? "" : lootPreview;
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
        if (!this.lootPreview.isEmpty()) {
            tag.putString("loot_preview", this.lootPreview);
        }
    }

    public static WeightData load(CompoundTag tag) {
        int w = tag.contains("weight") ? tag.getInt("weight") : defaultWeight();
        String preview = tag.contains("loot_preview") ? tag.getString("loot_preview") : "";
        return new WeightData(w, preview);
    }
}

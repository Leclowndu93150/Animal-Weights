package com.leclowndu93150.animalweights.config;

public final class AnimalProfile {
    static final AnimalProfile EMPTY = new AnimalProfile();

    public Diet diet;
    public Integer minWeight;
    public Integer maxWeight;
    public Integer defaultWeight;
    public Integer sickThreshold;
    public Integer weightTickIntervalTicks;
    public Double weightGainChance;
    public Double weightMinorLossChance;
    public Double weightSevereLossChance;

    private transient WeightStats stats;

    public AnimalProfile() {
    }

    public AnimalProfile(Diet diet) {
        this.diet = diet;
    }

    WeightStats stats(AnimalWeightsConfig config) {
        WeightStats resolved = this.stats;
        if (resolved == null) {
            resolved = WeightStats.of(config, this);
            this.stats = resolved;
        }
        return resolved;
    }
}

package com.leclowndu93150.animalweights.config;

public record WeightStats(
    int minWeight,
    int maxWeight,
    int defaultWeight,
    int sickThreshold,
    int weightTickIntervalTicks,
    double weightGainChance,
    double weightMinorLossChance,
    double weightSevereLossChance
) {
    static WeightStats of(AnimalWeightsConfig config, AnimalProfile profile) {
        int min = Math.max(0, pick(profile.minWeight, config.minWeight));
        int max = Math.max(min + 1, pick(profile.maxWeight, config.maxWeight));
        int initial = Math.max(min, Math.min(max, pick(profile.defaultWeight, config.defaultWeight)));
        return new WeightStats(
            min,
            max,
            initial,
            pick(profile.sickThreshold, config.sickThreshold),
            Math.max(20, pick(profile.weightTickIntervalTicks, config.weightTickIntervalTicks)),
            pick(profile.weightGainChance, config.weightGainChance),
            pick(profile.weightMinorLossChance, config.weightMinorLossChance),
            pick(profile.weightSevereLossChance, config.weightSevereLossChance)
        );
    }

    private static int pick(Integer override, int fallback) {
        return override != null ? override : fallback;
    }

    private static double pick(Double override, double fallback) {
        return override != null ? override : fallback;
    }

    public int clamp(int weight) {
        return Math.max(minWeight, Math.min(maxWeight, weight));
    }

    public boolean isSick(int weight) {
        return weight <= sickThreshold;
    }
}

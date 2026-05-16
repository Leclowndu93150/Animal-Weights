package com.leclowndu93150.animalweights.config;

public enum ScalingMode {
    MULTIPLICATIVE,
    ADDITIVE;

    public static ScalingMode parse(String value, ScalingMode fallback) {
        if (value == null) return fallback;
        try {
            return ScalingMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public int apply(int base, int weight) {
        if (weight <= 1) return base;
        return this == MULTIPLICATIVE ? base * weight : base + (weight - 1);
    }

    public int extraRolls(int weight) {
        if (weight <= 1) return 0;
        return this == MULTIPLICATIVE ? (weight - 1) : 1;
    }
}

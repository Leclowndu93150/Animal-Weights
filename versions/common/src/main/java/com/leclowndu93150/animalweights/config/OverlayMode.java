package com.leclowndu93150.animalweights.config;

public enum OverlayMode {
    ALWAYS,
    LOOKING,
    CROUCH_LOOKING,
    NEARBY;

    public static OverlayMode parse(String value, OverlayMode fallback) {
        if (value == null) return fallback;
        try {
            return OverlayMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}

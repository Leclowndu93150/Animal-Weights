package com.leclowndu93150.animalweights.config;

public final class AnimalWeightsConfig {
    public ScalingMode dropScalingMode = ScalingMode.MULTIPLICATIVE;
    public ScalingMode xpScalingMode = ScalingMode.MULTIPLICATIVE;

    public int minWeight = 0;
    public int defaultWeight = 1;
    public int maxWeight = 8;

    public int weightTickIntervalTicks = 8000;
    public double weightGainChance = 0.5;
    public double weightMinorLossChance = 0.25;
    public double weightSevereLossChance = 0.5;

    public int lightThreshold = 14;
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

    public AnimalWeightsConfig() {
    }
}

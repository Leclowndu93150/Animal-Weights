package com.leclowndu93150.animalweights.config;

public enum WaterSource {
    ANY,
    NATURAL_ONLY,
    CAULDRON_ONLY;

    public boolean acceptsNatural(Diet diet) {
        return this != CAULDRON_ONLY || diet == Diet.AQUATIC;
    }

    public boolean acceptsCauldron() {
        return this != NATURAL_ONLY;
    }
}

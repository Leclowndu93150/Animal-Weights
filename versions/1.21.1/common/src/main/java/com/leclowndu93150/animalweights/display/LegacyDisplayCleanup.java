package com.leclowndu93150.animalweights.display;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public final class LegacyDisplayCleanup {
    private static final String LEGACY_OVERLAY_TAG = "animalweights:overlay";

    private LegacyDisplayCleanup() {
    }

    public static void cleanup(Entity parent) {
        for (Entity passenger : new ArrayList<>(parent.getPassengers())) {
            if (passenger instanceof Display && passenger.getTags().contains(LEGACY_OVERLAY_TAG)) {
                passenger.discard();
            }
        }
        for (Display display : parent.level().getEntitiesOfClass(Display.class, parent.getBoundingBox().inflate(2.0), LegacyDisplayCleanup::isLegacyOverlay)) {
            display.discard();
        }
    }

    private static boolean isLegacyOverlay(Display display) {
        return display.getTags().contains(LEGACY_OVERLAY_TAG);
    }
}

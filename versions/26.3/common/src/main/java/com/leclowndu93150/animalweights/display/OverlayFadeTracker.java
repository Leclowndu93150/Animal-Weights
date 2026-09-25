package com.leclowndu93150.animalweights.display;

import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class OverlayFadeTracker {
    private static final long FADE_IN_MS = 1000L;
    private static final long FADE_OUT_MS = 1000L;
    private static final int PRUNE_THRESHOLD = 256;
    private static final Map<Integer, Entry> ENTRIES = new HashMap<>();

    private OverlayFadeTracker() {
    }

    public static void clear(Animal animal) {
        ENTRIES.remove(animal.getId());
    }

    public static float alpha(Animal animal, boolean visible, int weight) {
        int id = animal.getId();
        if (animal.isRemoved()) {
            ENTRIES.remove(id);
            return 0.0F;
        }

        long now = System.currentTimeMillis();
        prune(now);

        Entry entry = ENTRIES.get(id);
        if (entry != null && (!entry.uuid.equals(animal.getUUID()) || entry.weight != weight)) {
            ENTRIES.remove(id);
            entry = null;
        }

        if (visible && entry == null) {
            entry = new Entry(animal.getUUID(), weight, now);
            ENTRIES.put(id, entry);
        }
        if (entry == null) {
            return 0.0F;
        }

        if (visible) {
            if (entry.fadeOutStartedAtMs != 0L) {
                float currentAlpha = fadeOutAlpha(entry, now);
                entry.startedAtMs = now - (long) (currentAlpha * FADE_IN_MS);
                entry.fadeOutStartedAtMs = 0L;
                entry.fadeOutStartAlpha = 1.0F;
            }
            return smooth(Math.min(1.0F, (float) (now - entry.startedAtMs) / (float) FADE_IN_MS));
        }

        if (entry.fadeOutStartedAtMs == 0L) {
            entry.fadeOutStartAlpha = smooth(Math.min(1.0F, (float) (now - entry.startedAtMs) / (float) FADE_IN_MS));
            entry.fadeOutStartedAtMs = now;
        }
        float alpha = fadeOutAlpha(entry, now);
        if (alpha <= 0.0F) {
            ENTRIES.remove(id);
            return 0.0F;
        }
        return alpha;
    }

    private static float fadeOutAlpha(Entry entry, long now) {
        float progress = Math.min(1.0F, (float) (now - entry.fadeOutStartedAtMs) / (float) FADE_OUT_MS);
        return entry.fadeOutStartAlpha * smooth(1.0F - progress);
    }

    private static float smooth(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static void prune(long now) {
        if (ENTRIES.size() < PRUNE_THRESHOLD) {
            return;
        }
        Iterator<Map.Entry<Integer, Entry>> iterator = ENTRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next().getValue();
            if (entry.fadeOutStartedAtMs != 0L && now - entry.fadeOutStartedAtMs > FADE_OUT_MS) {
                iterator.remove();
            }
        }
    }

    private static final class Entry {
        private final UUID uuid;
        private final int weight;
        private long startedAtMs;
        private long fadeOutStartedAtMs;
        private float fadeOutStartAlpha = 1.0F;

        private Entry(UUID uuid, int weight, long startedAtMs) {
            this.uuid = uuid;
            this.weight = weight;
            this.startedAtMs = startedAtMs;
        }
    }
}

package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.resources.ResourceLocation;

public final class LootSyncPayload {
    public static final ResourceLocation ENTRY_CHANNEL = new ResourceLocation(Animalweights.MOD_ID, "loot_entry");
    public static final ResourceLocation SNAPSHOT_CHANNEL = new ResourceLocation(Animalweights.MOD_ID, "loot_snapshot");

    private LootSyncPayload() {
    }
}

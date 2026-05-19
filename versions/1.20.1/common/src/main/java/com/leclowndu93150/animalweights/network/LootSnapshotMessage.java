package com.leclowndu93150.animalweights.network;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class LootSnapshotMessage {
    public final Map<ResourceLocation, List<ResourceLocation>> entries;

    public LootSnapshotMessage(Map<ResourceLocation, List<ResourceLocation>> entries) {
        this.entries = entries;
    }
}

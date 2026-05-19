package com.leclowndu93150.animalweights.network;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LootEntryMessage {
    public final ResourceLocation entityType;
    public final List<ResourceLocation> items;

    public LootEntryMessage(ResourceLocation entityType, List<ResourceLocation> items) {
        this.entityType = entityType;
        this.items = items;
    }
}

package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class LootEntryPayload implements CustomPacketPayload {
    public static final Type<LootEntryPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Animalweights.MOD_ID, "loot_entry")
    );

    public static final StreamCodec<FriendlyByteBuf, LootEntryPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeResourceLocation(payload.entityType);
            buf.writeVarInt(payload.items.size());
            for (ResourceLocation id : payload.items) buf.writeResourceLocation(id);
        },
        buf -> {
            ResourceLocation type = buf.readResourceLocation();
            int count = buf.readVarInt();
            List<ResourceLocation> items = new ArrayList<>(count);
            for (int i = 0; i < count; i++) items.add(buf.readResourceLocation());
            return new LootEntryPayload(type, items);
        }
    );

    public final ResourceLocation entityType;
    public final List<ResourceLocation> items;

    public LootEntryPayload(ResourceLocation entityType, List<ResourceLocation> items) {
        this.entityType = entityType;
        this.items = items;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

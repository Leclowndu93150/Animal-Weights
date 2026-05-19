package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class LootEntryPayload implements CustomPacketPayload {
    public static final Type<LootEntryPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "loot_entry")
    );

    public static final StreamCodec<FriendlyByteBuf, LootEntryPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeIdentifier(payload.entityType);
            buf.writeVarInt(payload.items.size());
            for (Identifier id : payload.items) buf.writeIdentifier(id);
        },
        buf -> {
            Identifier type = buf.readIdentifier();
            int count = buf.readVarInt();
            List<Identifier> items = new ArrayList<>(count);
            for (int i = 0; i < count; i++) items.add(buf.readIdentifier());
            return new LootEntryPayload(type, items);
        }
    );

    public final Identifier entityType;
    public final List<Identifier> items;

    public LootEntryPayload(Identifier entityType, List<Identifier> items) {
        this.entityType = entityType;
        this.items = items;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

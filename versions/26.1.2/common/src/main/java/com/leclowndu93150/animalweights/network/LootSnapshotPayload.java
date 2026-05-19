package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootSnapshotPayload implements CustomPacketPayload {
    public static final Type<LootSnapshotPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "loot_snapshot")
    );

    public static final StreamCodec<FriendlyByteBuf, LootSnapshotPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.entries.size());
            for (Map.Entry<Identifier, List<Identifier>> entry : payload.entries.entrySet()) {
                buf.writeIdentifier(entry.getKey());
                buf.writeVarInt(entry.getValue().size());
                for (Identifier id : entry.getValue()) buf.writeIdentifier(id);
            }
        },
        buf -> {
            int count = buf.readVarInt();
            Map<Identifier, List<Identifier>> entries = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                Identifier type = buf.readIdentifier();
                int n = buf.readVarInt();
                List<Identifier> items = new ArrayList<>(n);
                for (int j = 0; j < n; j++) items.add(buf.readIdentifier());
                entries.put(type, items);
            }
            return new LootSnapshotPayload(entries);
        }
    );

    public final Map<Identifier, List<Identifier>> entries;

    public LootSnapshotPayload(Map<Identifier, List<Identifier>> entries) {
        this.entries = entries;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootSnapshotPayload implements CustomPacketPayload {
    public static final Type<LootSnapshotPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Animalweights.MOD_ID, "loot_snapshot")
    );

    public static final StreamCodec<FriendlyByteBuf, LootSnapshotPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.entries.size());
            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : payload.entries.entrySet()) {
                buf.writeResourceLocation(entry.getKey());
                buf.writeVarInt(entry.getValue().size());
                for (ResourceLocation id : entry.getValue()) buf.writeResourceLocation(id);
            }
        },
        buf -> {
            int count = buf.readVarInt();
            Map<ResourceLocation, List<ResourceLocation>> entries = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                ResourceLocation type = buf.readResourceLocation();
                int n = buf.readVarInt();
                List<ResourceLocation> items = new ArrayList<>(n);
                for (int j = 0; j < n; j++) items.add(buf.readResourceLocation());
                entries.put(type, items);
            }
            return new LootSnapshotPayload(entries);
        }
    );

    public final Map<ResourceLocation, List<ResourceLocation>> entries;

    public LootSnapshotPayload(Map<ResourceLocation, List<ResourceLocation>> entries) {
        this.entries = entries;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

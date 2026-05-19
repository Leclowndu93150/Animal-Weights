package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class WeightSyncPayload implements CustomPacketPayload {
    public static final Type<WeightSyncPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Animalweights.MOD_ID, "weight_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, WeightSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.entityId);
            buf.writeVarInt(payload.weight);
        },
        buf -> new WeightSyncPayload(buf.readVarInt(), buf.readVarInt())
    );

    public final int entityId;
    public final int weight;

    public WeightSyncPayload(int entityId, int weight) {
        this.entityId = entityId;
        this.weight = weight;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

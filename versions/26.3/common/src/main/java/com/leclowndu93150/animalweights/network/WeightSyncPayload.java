package com.leclowndu93150.animalweights.network;

import com.leclowndu93150.animalweights.Animalweights;
import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;

public final class WeightSyncPayload implements CustomPacketPayload {
    public static final Type<WeightSyncPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "weight_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, WeightSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.entityId);
            buf.writeVarInt(payload.weight);
            buf.writeBoolean(payload.tracked);
        },
        buf -> new WeightSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
    );

    public final int entityId;
    public final int weight;
    public final boolean tracked;

    public WeightSyncPayload(int entityId, int weight, boolean tracked) {
        this.entityId = entityId;
        this.weight = weight;
        this.tracked = tracked;
    }

    public static WeightSyncPayload of(Animal animal) {
        return new WeightSyncPayload(animal.getId(), WeightAttachment.getWeight(animal), AnimalWeightsRules.isActive(animal));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

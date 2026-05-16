package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public final class WeightData {
    public static int minWeight() {
        return ConfigManager.get().minWeight;
    }

    public static int defaultWeight() {
        return ConfigManager.get().defaultWeight;
    }

    public static int maxWeight() {
        return ConfigManager.get().maxWeight;
    }

    public static final MapCodec<WeightData> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("weight", 1).forGetter(d -> d.weight),
        Codec.STRING.optionalFieldOf("loot_preview", "").forGetter(d -> d.lootPreview)
    ).apply(i, WeightData::new));

    public static final Codec<WeightData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<ByteBuf, WeightData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, d -> d.weight,
        ByteBufCodecs.STRING_UTF8, d -> d.lootPreview,
        WeightData::new
    );

    private int weight;
    private String lootPreview;
    private long bonusCacheTick = Long.MIN_VALUE;
    private boolean bonusCacheValue;

    public WeightData() {
        this(defaultWeight(), "");
    }

    public WeightData(int weight, String lootPreview) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
        this.lootPreview = lootPreview == null ? "" : lootPreview;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        this.weight = Mth.clamp(weight, cfg.minWeight, cfg.maxWeight);
    }

    public String getLootPreview() {
        return lootPreview;
    }

    public void setLootPreview(String lootPreview) {
        this.lootPreview = lootPreview == null ? "" : lootPreview;
    }

    public long getBonusCacheTick() {
        return bonusCacheTick;
    }

    public boolean getBonusCacheValue() {
        return bonusCacheValue;
    }

    public void storeBonusCache(long tick, boolean value) {
        this.bonusCacheTick = tick;
        this.bonusCacheValue = value;
    }
}

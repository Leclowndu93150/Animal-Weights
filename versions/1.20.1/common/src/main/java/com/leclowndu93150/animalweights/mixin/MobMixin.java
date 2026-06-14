package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.WeightData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(Mob.class)
public abstract class MobMixin {
    private static final EnumSet<MobSpawnType> ANIMALWEIGHTS$NATURAL = EnumSet.of(
        MobSpawnType.NATURAL,
        MobSpawnType.CHUNK_GENERATION,
        MobSpawnType.STRUCTURE,
        MobSpawnType.PATROL,
        MobSpawnType.JOCKEY
    );

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void animalweights$markNaturalSpawn(
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType reason,
        @Nullable SpawnGroupData groupData,
        @Nullable CompoundTag tag,
        CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        if (!ANIMALWEIGHTS$NATURAL.contains(reason)) return;
        Object self = this;
        if (!(self instanceof Animal animal)) return;
        WeightData data = WeightAttachment.get(animal);
        data.setNaturallySpawned(true);
    }
}

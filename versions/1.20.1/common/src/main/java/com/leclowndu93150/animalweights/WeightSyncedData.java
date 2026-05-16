package com.leclowndu93150.animalweights;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.Animal;

public final class WeightSyncedData {
    public static final EntityDataAccessor<Integer> WEIGHT =
        SynchedEntityData.defineId(Animal.class, EntityDataSerializers.INT);

    private WeightSyncedData() {
    }
}

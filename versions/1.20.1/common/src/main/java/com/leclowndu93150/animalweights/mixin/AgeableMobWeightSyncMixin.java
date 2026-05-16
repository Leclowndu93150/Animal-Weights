package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.WeightData;
import com.leclowndu93150.animalweights.WeightHolder;
import com.leclowndu93150.animalweights.WeightSyncedData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AgeableMobWeightSyncMixin {
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void animalweights$defineWeight(CallbackInfo ci) {
        AgeableMob self = (AgeableMob) (Object) this;
        if (self instanceof Animal) {
            self.getEntityData().define(WeightSyncedData.WEIGHT, WeightData.defaultWeight());
        }
    }

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("TAIL"))
    private void animalweights$onSync(EntityDataAccessor<?> key, CallbackInfo ci) {
        AgeableMob self = (AgeableMob) (Object) this;
        if (self instanceof Animal animal && WeightSyncedData.WEIGHT.equals(key) && animal.level().isClientSide) {
            int synced = animal.getEntityData().get(WeightSyncedData.WEIGHT);
            ((WeightHolder) animal).animalweights$setWeightData(new WeightData(synced, ""));
        }
    }
}

package com.leclowndu93150.animalweights.mixin;

import com.leclowndu93150.animalweights.SickState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements SickState {
    @Unique
    private boolean animalweights$sick;

    @Override
    public boolean animalweights$isSick() {
        return this.animalweights$sick;
    }

    @Override
    public void animalweights$setSick(boolean sick) {
        this.animalweights$sick = sick;
    }
}

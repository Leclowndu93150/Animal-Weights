package com.leclowndu93150.animalweights.mixin;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccessor {
    @Invoker("setBillboardConstraints")
    void animalweights$setBillboardConstraints(Display.BillboardConstraints constraints);

    @Invoker("setViewRange")
    void animalweights$setViewRange(float range);

    @Invoker("setTransformation")
    void animalweights$setTransformation(Transformation transformation);
}

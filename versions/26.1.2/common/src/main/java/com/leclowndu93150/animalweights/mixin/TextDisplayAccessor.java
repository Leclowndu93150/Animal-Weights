package com.leclowndu93150.animalweights.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Invoker("setText")
    void animalweights$setText(Component text);

    @Invoker("setBackgroundColor")
    void animalweights$setBackgroundColor(int color);

    @Invoker("setFlags")
    void animalweights$setFlags(byte flags);
}

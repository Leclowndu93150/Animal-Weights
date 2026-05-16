package com.leclowndu93150.animalweights;

import net.minecraft.world.entity.Entity;

import java.util.List;

public interface DisplayTracker {
    List<Entity> animalweights$displays();
    void animalweights$trackDisplay(Entity display);
    void animalweights$clearDisplays();
}

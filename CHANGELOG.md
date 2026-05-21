# 1.0.5

Animals now pause their weight cycle at night. They rest until dawn, don't lose weight while you're sleeping, and the magnifying glass / Jade tooltip shows "Resting until dawn" instead of a countdown. Disable via `pauseAtNight: false` in the config.

Added diet groups. Carnivores (wolf, cat, ocelot, polar bear, frog) no longer need grazing; they get a free point in place of it. Aquatic mobs (axolotl, dolphin, all fish) need water and ignore grass. Omnivores (chicken, panda, fox, bee, turtle, hoglin, sniffer, armadillo) need light, space, and either water or grass. Herbivores still need all four (light/water/grass/space). Modded animals default to omnivore so they're not punished for unfamiliar habitats. Override per entity ID via the `entityDiets` config map.

Added `disabledEntities` config option to fully exclude entity types from the system. Useful for cosmetic pets like Quark's Shiba Inus that have no loot worth scaling. Disabled mobs get no weight tracking, no sick tint, no breeding block, no drop scaling, no Jade tooltip, no chat output from the magnifying glass. Example:

```
"disabledEntities": ["quark:shiba"]
```

# 1.0.4

- Added a Magnifying Glass item (craft with spyglass + paper) that prints an animal's weight, sick status, habitat checks, and time until the next check. Lives in a new Animal Weights creative tab.
- Added Jade compat: hover over an animal to see the same info with a live countdown.
- Fixed animals not gaining weight even with all conditions met. Each animal now has its own counter that survives chunk reloads.
- Fixed torches being ignored. Lowered default light threshold from 15 to 10. Existing configs need lightThreshold updated manually or animalweights.json deleted to regenerate.
- Habitat light check now uses the brightest of sun or block light. Daytime pastures pass.
- Cheaper wander-to-habitat AI. Big farms no longer eat noticeable tick budget.
- Crowding matches by entity type instead of exact Java class, so modded variants count.
- Added sickThreshold config (default 0), so raising minWeight above 0 no longer makes everything permanently sick.
- Reworked config with comments.
- Reworked overlay display to fade in/out when looking at an entity.

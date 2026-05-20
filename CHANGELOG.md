Added a Magnifying Glass item that shows an animal's condition when you right-click it (weight, sick status, habitat checks for light/water/grazing/space, and how long until the next check). Craft it from a spyglass and paper. The item lives in a new Animal Weights creative tab.

Added Jade compat. Hover over any animal with Jade installed to see the same condition info, including a live countdown to the next weight check.

Fixed animals not gaining weight even with all conditions met. The internal check that runs every weight cycle was tied to global game time and could be skipped if the chunk unloaded for any moment, leaving mobs stuck. Each animal now has its own counter that survives chunk reloads.

Fixed torches being ignored as a light source. The default light threshold was 15, which only a torch's own block met. Lowered to 10 so torches actually count at reasonable spacing. If you already have a config file, edit lightThreshold to 10 or delete animalweights.json to regenerate.

Habitat detection now uses the brightest of sun or block light. Outdoor pastures during the day no longer fail the light check.

Made the wander-to-habitat AI cheaper. Animals still seek out better spots but the goal no longer eats a noticeable chunk of server tick budget on big farms.

Crowding now matches species by entity type instead of exact Java class, so modded variants of vanilla animals count correctly.

Added a separate sick threshold to the config (sickThreshold, default 0) so you can raise minWeight above 0 without every animal becoming permanently sick.


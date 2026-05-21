# Animal Weights

Animals gain weight from good farm conditions. Healthy mobs drop more loot when killed. Mistreated ones get sick and stop breeding. Concept by [u/Axoladdy](https://www.reddit.com/r/Minecraft/comments/1te0m69/animal_weights_a_simple_mechanic_to_encourage/)

## What it does

Every passive mob (cow, pig, sheep, chicken, rabbit, polar bear, axolotl, and any modded animal) tracks a hidden weight value from 1 to 8. By default mobs start at 1 and drop normal loot. As conditions around them change, the weight goes up or down, and so does what they drop on death.

Sick mobs (weight 0) turn green, can't be bred, and float a "Sick" label above their head.

Mobs with weight 2 or higher display their bonus drops above their head: an icon of each loot item with a "+N" next to it, showing exactly how many extra rolls you'll get on death.

## How weight changes

Every in-game hour or so, each animal checks four things about its surroundings. **The check is skipped at night**: animals rest until dawn and don't lose weight while you're sleeping or wandering around with a torch.

The check is also diet-aware. Carnivores like wolves and polar bears don't care about grass, aquatic animals don't care about land, and so on.

The four checks for an herbivore (cow, sheep, pig, horse, rabbit, etc.) are:

1.  Light level of 10 or higher (block light from torches, or sunlight)
2.  Water within 4 blocks
3.  Grass or moss block within 4 blocks
4.  Fewer than 7 same-species mobs in a 5x5 area

Carnivores (wolf, polar bear, ocelot, frog, cat) trade the grazing requirement for a free point: they just need light, water, and space.

Aquatic mobs (axolotl, dolphin, all fish) need light, water (counts double), and space. Grass is irrelevant.

Omnivores (chicken, panda, fox, bee, turtle) need light, space, and either water or grass.

Modded animals fall under the configurable `defaultDiet` (omnivore by default, relaxed checks so unfamiliar mobs don't suffer). You can override per-entity-type in the config.

The score determines what happens to weight:

*   4 out of 4: 50% chance to gain a point
*   3 out of 4: no change
*   2 out of 4: 25% chance to lose a point
*   0 or 1 out of 4: 50% chance to lose a point

Hit 0 and the animal is sick. It can't breed and it tints green until conditions improve.

## How drops change

Weight multiplies the loot table rolls. A cow at weight 5 will drop the loot table 5 times instead of once. Looting, Luck, and any other enchantments apply normally to every roll, and each roll is independently randomized.

XP works the same way: base XP gets multiplied by weight.

Passive mobs also get a small bonus when killed near water or inside a village (one extra loot roll, +1 XP), regardless of weight. Monsters are unaffected.

## Magnifying Glass

Craft a Magnifying Glass with a spyglass and paper (or grab one from the new Animal Weights creative tab). Right-click an animal to see its current weight, sick status, habitat checks (light / water / grazing / space), and how long until the next weight evaluation.

If you have [Jade](https://www.curseforge.com/minecraft/mc-mods/jade) installed, hovering over any animal shows the same info as a tooltip with a live countdown. No item needed.

## Excluding mobs

Decorative pets or mobs that don't drop anything worth scaling (Quark's Shiba Inus, for example) can be opted out entirely. Add their entity ID to `disabledEntities` in the config:

```json
"disabledEntities": ["quark:shiba", "examplemod:goldfish"]
```

Disabled mobs are completely ignored: no weight tracking, no sick tint, no breeding block, no Jade tooltip, no drop scaling.

## Commands

`/animalweights` while looking at an entity prints its current weight, loot preview, and tracked overlays.

`/animalweights set <0-8>` sets the weight of the entity you're looking at. Use it for testing or debugging.

`/animalweights set <0-8> <entity selector>` sets weight on selected entities.

Requires permission level 2 (gamemaster).

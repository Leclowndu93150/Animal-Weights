# 1.2.1

Hoglins can finally put on weight. They were treated as omnivores, which meant their weight cycle paused in the Nether, and anywhere else they turn into zoglins within seconds. They now use the Nether diet like Striders: lava and nylium or netherrack nearby, in the Nether. Hoglins already saved as omnivores in `animaldiets.json` are corrected automatically. (Closes #14)

The Jade tooltip now tells you when an animal is still wild. Previously it could show a full set of habitat checks for a wild animal that wasn't being tracked at all, making it look like it should be gaining weight when it never would. It now shows "Wild — leash or breed it to start tracking weight" until you leash or breed it. (Closes #11)

Picking up a spyglass now unlocks the Magnifying Glass recipe in your recipe book. (Closes #10)

Everything the Magnifying Glass and the Jade tooltip say can now be translated, and animal names in the Magnifying Glass readout show up in your own language. (Closes #12)

Added a Brazilian Portuguese translation, contributed by McThaWeb. (Closes #13)

The loot overlay and the sick tint can now require an item in hand. List item IDs in the new `overlayRequiredItems` option, for example `["animalweights:magnifying_glass"]`, and players only see the floating loot overlay and the green sick tint while holding one of them in either hand. Empty by default, so nothing changes unless you set it. (Closes #15)

# 1.2.0

Added support for Minecraft 26.3 (Fabric and NeoForge).

You can now make cauldrons the only water source. The new `waterSource` option decides what counts as water for your animals: `ANY` (water blocks or a water cauldron, the default), `NATURAL_ONLY` (water blocks only), or `CAULDRON_ONLY` (only water cauldrons, so every weight gain drains the cauldron and someone has to keep it topped up). Aquatic animals still count the water they swim in, so fish and axolotls don't get sick in `CAULDRON_ONLY` mode. Animals looking for a better spot to stand now also head for nearby water cauldrons. This replaces the old `cauldronCountsAsWater` option, and your existing setting is carried over automatically.

Only want the mod on your farm animals? The new `LIVESTOCK` filter mode tracks cows, mooshrooms, pigs, sheep, chickens, goats and rabbits, and leaves pets and modded companions completely alone. It still respects `enabledEntities` and `disabledEntities`, so you can add or remove animals on top of it. The list is the `animalweights:livestock` entity tag, which you can extend with a datapack. As a reminder, `entityFilterMode` also offers `BLACKLIST`, `WHITELIST` and `VANILLA_ONLY` if you'd rather hand-pick which animals are tracked.

Animals can now have their own weight stats. Each entry in `animaldiets.json` can set its own `maxWeight`, `minWeight`, `defaultWeight`, `sickThreshold`, `weightTickIntervalTicks` and gain/loss chances next to its diet, for example `"minecraft:strider": {"diet": "NETHER", "maxWeight": 4}`. Anything you leave out uses the global value from `animalweights.json`. The top of `animaldiets.json` now explains every option and what values it accepts. Your existing diet entries are converted to the new format automatically the first time you load the update.

Added support for Animal Feeding Trough. A feeding trough stocked with food an animal eats now counts as grazing ground for herbivores and omnivores, so you can raise them in a barn without grass. Each weight gain that relied on the trough eats one item from it. The magnifying glass and the Jade readout take troughs into account too. You can turn this off with `feedingTroughCountsAsGrazing`. The old `cauldronScanRadius` is now `feederScanRadius` and covers both cauldrons and troughs;

`sickThreshold` now sticks. It was missing from the saved config, so any change you made to it was lost the next time the config was saved. It now appears in `animalweights.json` along with the rest of the settings.

One bad entry in `animaldiets.json` no longer wipes the whole file. A broken entry is now skipped (and reported in the log) while the rest of your entries are kept.
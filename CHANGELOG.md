# 1.0.8

Added support for Minecraft 26.2 (Fabric and NeoForge).

Wild animals are now left alone until you make them yours. Naturally-spawned animals no longer gain weight, scale their loot, or show a weight readout while they're still wild — they behave exactly like vanilla. The moment you leash one or breed it, it starts being tracked (and stays tracked from then on). This keeps big wild herds cheap to run and means you can't harvest buffed loot off animals you never actually raised. Bred, spawn-egg, and spawner animals are considered yours from the start, as before. You can turn this back off with `requireEngagement` if you want every animal tracked like older versions. Note: any wild animal you were already tracking before this update will go back to vanilla until you leash or breed it.

Your config changes now stick between play sessions. Previously, editing `animalweights.json` and then reloading your world would silently revert your changes. The config is now re-read from disk every time a world loads, so any edit you make outside of the game is applied before you play. This affected every version, but was reported on 1.20.1. (Closes #5.)

Fixed a crash with Jade on newer versions. The mod's Jade tooltip integration was failing to load on Minecraft 1.21.6 and up, so animal condition info never showed up. It now loads correctly again. (Closes #7.)

Grass detection now works with modded packs. Packs like TerraFirmaCraft use their own grass and moss blocks, which the mod didn't recognise — leaving those animals permanently sick with no way to fix it. Two new config options let you tell the mod what else counts as grazing ground: `grazingBlocks` for specific blocks (e.g. `"tfc:grass/loam"`) and `grazingBlockTags` for block tags (e.g. `"c:grass_blocks"`). Vanilla grass and moss still count out of the box. (Closes #6.)

Animals now need room to roam. Sealing a cow in a one-block hole with a torch and a splash of water it can't even reach used to be enough to fatten it up. Animals now also need open space around them to be happy, so cramped pens no longer count as a good habitat. You can turn this off with `requireOpenSpace` or tune how much space is needed with `minOpenSpace`. (Addresses #4.)

Nether animals can finally put on weight. Striders and other Nether-diet mobs needed a bright light level to thrive, which the Nether almost never provides, so they were permanently stuck losing weight even in a perfect lava-lake home. They no longer depend on light — being over lava on nether ground is enough now.

Nether animals can optionally fatten up anywhere. A new `netherAnimalsGainAnywhere` option (off by default) lets Striders and the like gain weight outside the Nether, so you can keep them in an Overworld pen as long as you give them lava and nylium to stand near.

Mooshrooms (and anything grazing on mushroom ground) stopped starving. Mycelium and podzol now count as grazing ground alongside grass and moss, so Mooshrooms on their home island no longer slowly go sick.

The magnifying glass and Jade readout now show the right habitat needs per animal. Nether animals show Lava and Nylium instead of Water and Grazing, aquatic animals just show Water, and the readout correctly predicts whether an animal will gain, hold, or lose — previously it always assumed a grass-and-water diet.

Animal diets now live in their own config file. Diets have moved out of `animalweights.json` and into a new `animaldiets.json`. Your existing diet overrides are carried over automatically the first time you load the update, and newly discovered mobs are added to the new file as you encounter them — nothing to do by hand. (Closes #8, contributed by TorranTR.)

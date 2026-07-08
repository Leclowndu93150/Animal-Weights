package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.inspect.MagnifyingGlassItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AnimalweightsItems {
    private static final ResourceKey<Item> MAGNIFYING_GLASS_KEY = ResourceKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "magnifying_glass"));

    public static final Item MAGNIFYING_GLASS = new MagnifyingGlassItem(new Item.Properties().stacksTo(1).setId(MAGNIFYING_GLASS_KEY));
    public static final CreativeModeTab MAIN_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
        .title(Component.translatable("itemGroup.animalweights.main"))
        .icon(() -> new ItemStack(MAGNIFYING_GLASS))
        .displayItems((params, output) -> output.accept(MAGNIFYING_GLASS))
        .build();

    private AnimalweightsItems() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, MAGNIFYING_GLASS_KEY, MAGNIFYING_GLASS);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Animalweights.MOD_ID, "main"), MAIN_TAB);
    }
}

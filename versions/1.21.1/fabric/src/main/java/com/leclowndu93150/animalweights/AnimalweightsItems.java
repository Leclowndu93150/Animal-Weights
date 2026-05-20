package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.inspect.MagnifyingGlassItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AnimalweightsItems {
    public static final Item MAGNIFYING_GLASS = new MagnifyingGlassItem(new Item.Properties().stacksTo(1));
    public static final CreativeModeTab MAIN_TAB = FabricItemGroup.builder()
        .title(Component.translatable("itemGroup.animalweights.main"))
        .icon(() -> new ItemStack(MAGNIFYING_GLASS))
        .displayItems((params, output) -> output.accept(MAGNIFYING_GLASS))
        .build();

    private AnimalweightsItems() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Animalweights.MOD_ID, "magnifying_glass"), MAGNIFYING_GLASS);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(Animalweights.MOD_ID, "main"), MAIN_TAB);
    }
}

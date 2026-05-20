package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.inspect.MagnifyingGlassItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class AnimalweightsItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Animalweights.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Animalweights.MOD_ID);

    public static final DeferredItem<Item> MAGNIFYING_GLASS = ITEMS.registerItem(
        "magnifying_glass",
        MagnifyingGlassItem::new,
        new Item.Properties().stacksTo(1)
    );

    public static final Supplier<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.animalweights.main"))
        .icon(() -> new ItemStack(MAGNIFYING_GLASS.get()))
        .displayItems((params, output) -> output.accept(MAGNIFYING_GLASS.get()))
        .build());

    private AnimalweightsItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}

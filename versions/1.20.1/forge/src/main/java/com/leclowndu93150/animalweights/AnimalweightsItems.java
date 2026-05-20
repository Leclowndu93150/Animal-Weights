package com.leclowndu93150.animalweights;

import com.leclowndu93150.animalweights.inspect.MagnifyingGlassItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AnimalweightsItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Animalweights.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Animalweights.MOD_ID);

    public static final RegistryObject<Item> MAGNIFYING_GLASS = ITEMS.register("magnifying_glass",
        () -> new MagnifyingGlassItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
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

package com.leclowndu93150.animalweights.display;

import com.leclowndu93150.animalweights.DisplayTracker;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.mixin.DisplayAccessor;
import com.leclowndu93150.animalweights.mixin.ItemDisplayAccessor;
import com.leclowndu93150.animalweights.mixin.TextDisplayAccessor;
import com.mojang.math.Transformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class DisplayOverlay {
    public static final String OVERLAY_TAG = "animalweights:overlay";

    private static final float ROW_HEIGHT = 0.55F;
    private static final float ICON_SCALE = 0.45F;
    private static final float TEXT_SCALE = 0.55F;
    private static final float TEXT_X_OFFSET = 0.45F;

    private DisplayOverlay() {
    }

    public static void refresh(Animal animal, ServerLevel level) {
        DisplayTracker tracker = (DisplayTracker) animal;
        int weight = WeightAttachment.getWeight(animal);
        boolean showBonus = weight > 1;
        boolean showSick = weight <= 0;

        despawnAll(animal, tracker);
        if (!showBonus && !showSick) {
            return;
        }

        if (showSick) {
            Display.TextDisplay sick = createTextDisplay(level,
                Component.literal("Sick").withStyle(ChatFormatting.GREEN),
                0.0F,
                ROW_HEIGHT);
            if (sick != null) {
                attach(animal, sick);
                tracker.animalweights$trackDisplay(sick);
            }
            return;
        }

        List<ItemStack> icons = LootSampler.decode(WeightAttachment.getLootPreview(animal));
        if (icons.isEmpty()) {
            List<Item> sampled = LootSampler.sample(animal, level);
            if (!sampled.isEmpty()) {
                WeightAttachment.setLootPreview(animal, LootSampler.encode(sampled));
                for (Item item : sampled) icons.add(new ItemStack(item));
            }
        }
        if (icons.isEmpty()) {
            return;
        }

        Component label = Component.literal("+" + (weight - 1)).withStyle(ChatFormatting.WHITE);
        for (int i = 0; i < icons.size(); i++) {
            float y = ROW_HEIGHT * (icons.size() - i);
            Display.ItemDisplay item = createItemDisplay(level, icons.get(i), -TEXT_X_OFFSET / 2.0F, y);
            if (item != null) {
                attach(animal, item);
                tracker.animalweights$trackDisplay(item);
            }
            Display.TextDisplay text = createTextDisplay(level, label, TEXT_X_OFFSET / 2.0F, y);
            if (text != null) {
                attach(animal, text);
                tracker.animalweights$trackDisplay(text);
            }
        }
    }

    private static Display.TextDisplay createTextDisplay(ServerLevel level, Component text, float x, float y) {
        Display.TextDisplay display = EntityType.TEXT_DISPLAY.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (display == null) return null;
        TextDisplayAccessor textAccess = (TextDisplayAccessor) display;
        textAccess.animalweights$setText(text);
        textAccess.animalweights$setBackgroundColor(0x00000000);
        DisplayAccessor displayAccess = (DisplayAccessor) display;
        displayAccess.animalweights$setBillboardConstraints(Display.BillboardConstraints.CENTER);
        displayAccess.animalweights$setViewRange(0.4F);
        displayAccess.animalweights$setTransformation(transformAt(x, y, TEXT_SCALE));
        return display;
    }

    private static Display.ItemDisplay createItemDisplay(ServerLevel level, ItemStack stack, float x, float y) {
        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (display == null) return null;
        ItemDisplayAccessor itemAccess = (ItemDisplayAccessor) display;
        itemAccess.animalweights$setItemStack(stack);
        itemAccess.animalweights$setItemTransform(ItemDisplayContext.GUI);
        DisplayAccessor displayAccess = (DisplayAccessor) display;
        displayAccess.animalweights$setBillboardConstraints(Display.BillboardConstraints.CENTER);
        displayAccess.animalweights$setViewRange(0.4F);
        displayAccess.animalweights$setTransformation(transformAt(x, y, ICON_SCALE));
        return display;
    }

    private static Transformation transformAt(float x, float y, float scale) {
        return new Transformation(
            new Vector3f(x, y, 0.0F),
            new Quaternionf(),
            new Vector3f(scale, scale, scale),
            new Quaternionf()
        );
    }

    private static void attach(Animal parent, Display display) {
        display.addTag(OVERLAY_TAG);
        display.snapTo(parent.getX(), parent.getY() + parent.getBbHeight() + 0.2, parent.getZ());
        parent.level().addFreshEntity(display);
        display.startRiding(parent, true, false);
    }

    private static void despawnAll(Animal parent, DisplayTracker tracker) {
        for (Entity e : new ArrayList<>(tracker.animalweights$displays())) {
            if (e != null && !e.isRemoved()) {
                e.discard();
            }
        }
        tracker.animalweights$clearDisplays();
        // Sweep any tagged passengers that aren't in the tracker (recovery from reload/desync).
        for (Entity passenger : new ArrayList<>(parent.getPassengers())) {
            if (passenger instanceof Display && passenger.getTags().contains(OVERLAY_TAG)) {
                passenger.discard();
            }
        }
    }

    public static void cleanup(Entity parent, DisplayTracker tracker) {
        for (Entity e : new ArrayList<>(tracker.animalweights$displays())) {
            if (e != null && !e.isRemoved()) {
                e.discard();
            }
        }
        tracker.animalweights$clearDisplays();
        for (Entity passenger : new ArrayList<>(parent.getPassengers())) {
            if (passenger instanceof Display && passenger.getTags().contains(OVERLAY_TAG)) {
                passenger.discard();
            }
        }
    }

    public static void onDeath(Entity parent, DisplayTracker tracker) {
        cleanup(parent, tracker);
    }
}

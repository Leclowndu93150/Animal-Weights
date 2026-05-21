package com.leclowndu93150.animalweights.inspect;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class MagnifyingGlassInspector {
    private static final Component CHECK = Component.literal("✓").withStyle(ChatFormatting.GREEN);
    private static final Component CROSS = Component.literal("✗").withStyle(ChatFormatting.RED);

    private MagnifyingGlassInspector() {
    }

    public static List<Component> buildChatLines(Animal animal) {
        if (AnimalWeightsRules.isDisabled(animal)) {
            return List.of();
        }
        Snapshot s = snapshot(animal, -1);
        List<Component> lines = new ArrayList<>(5);
        lines.add(Component.literal(animal.getType().getDescription().getString())
            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        lines.add(weightLine(s));
        if (s.sick) {
            lines.add(Component.literal("Sick — won't breed").withStyle(ChatFormatting.RED));
        }
        lines.add(checksLine(s));
        lines.add(nextLine(s));
        return lines;
    }

    public static List<Component> buildCompactLines(Animal animal) {
        return buildCompactLines(animal, -1);
    }

    public static List<Component> buildCompactLines(Animal animal, int elapsedTicksOverride) {
        if (AnimalWeightsRules.isDisabled(animal)) {
            return List.of();
        }
        Snapshot s = snapshot(animal, elapsedTicksOverride);
        List<Component> lines = new ArrayList<>(4);
        lines.add(weightLine(s));
        if (s.sick) {
            lines.add(Component.literal("Sick — won't breed").withStyle(ChatFormatting.RED));
        }
        lines.add(checksLine(s));
        lines.add(nextLine(s));
        return lines;
    }

    private static MutableComponent weightLine(Snapshot s) {
        return Component.literal("Weight ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(s.weight + "/" + s.maxWeight).withStyle(ChatFormatting.YELLOW));
    }

    private static MutableComponent checksLine(Snapshot s) {
        return Component.literal("Light ").withStyle(ChatFormatting.GRAY)
            .append(s.light ? CHECK : CROSS)
            .append(Component.literal("  Water ").withStyle(ChatFormatting.GRAY))
            .append(s.water ? CHECK : CROSS)
            .append(Component.literal("  Grazing ").withStyle(ChatFormatting.GRAY))
            .append(s.grazing ? CHECK : CROSS)
            .append(Component.literal("  Space ").withStyle(ChatFormatting.GRAY))
            .append(s.notCrowded ? CHECK : CROSS);
    }

    private static MutableComponent nextLine(Snapshot s) {
        if (s.sleeping) {
            return Component.literal("Resting until dawn")
                .withStyle(ChatFormatting.DARK_AQUA);
        }
        return Component.literal("Next ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(s.secondsUntilNext + "s").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" → ").withStyle(ChatFormatting.DARK_GRAY))
            .append(s.outcome);
    }

    private static Snapshot snapshot(Animal animal, int elapsedTicksOverride) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        Level level = animal.level();
        BlockPos pos = animal.blockPosition();
        Snapshot s = new Snapshot();
        s.weight = WeightAttachment.getWeight(animal);
        s.maxWeight = cfg.maxWeight;
        s.sick = s.weight <= cfg.sickThreshold;
        s.light = HabitatScanner.hasBrightLight(level, pos);
        s.water = HabitatScanner.hasWaterNearby(level, pos, cfg.habitatScanRadius);
        s.grazing = HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius);
        s.notCrowded = !HabitatScanner.isCrowded(animal);
        int score = (s.light ? 1 : 0) + (s.water ? 1 : 0) + (s.grazing ? 1 : 0) + (s.notCrowded ? 1 : 0);
        int interval = Math.max(1, cfg.weightTickIntervalTicks);
        int elapsed = elapsedTicksOverride >= 0 ? elapsedTicksOverride : WeightAttachment.get(animal).getTicksSinceEvaluation();
        int ticksUntilNext = Math.max(0, interval - elapsed);
        s.secondsUntilNext = ticksUntilNext / 20L;
        s.outcome = predictOutcome(score);
        s.sleeping = AnimalWeightsRules.isSleeping(level);
        return s;
    }

    private static Component predictOutcome(int score) {
        if (score >= 4) {
            return Component.literal("gain").withStyle(ChatFormatting.GREEN);
        }
        if (score == 3) {
            return Component.literal("stable").withStyle(ChatFormatting.WHITE);
        }
        if (score == 2) {
            return Component.literal("minor loss").withStyle(ChatFormatting.GOLD);
        }
        return Component.literal("severe loss").withStyle(ChatFormatting.RED);
    }

    private static final class Snapshot {
        int weight;
        int maxWeight;
        boolean sick;
        boolean light;
        boolean water;
        boolean grazing;
        boolean notCrowded;
        boolean sleeping;
        long secondsUntilNext;
        Component outcome;
    }
}

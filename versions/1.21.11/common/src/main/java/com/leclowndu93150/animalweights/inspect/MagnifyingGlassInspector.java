package com.leclowndu93150.animalweights.inspect;

import com.leclowndu93150.animalweights.AnimalWeightsRules;
import com.leclowndu93150.animalweights.WeightAttachment;
import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.Diet;
import com.leclowndu93150.animalweights.config.WeightStats;
import com.leclowndu93150.animalweights.habitat.HabitatScanner;
import com.leclowndu93150.animalweights.habitat.WeightTickLogic;
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
    private static final Component SEPARATOR = Component.literal("  ");

    private MagnifyingGlassInspector() {
    }

    public static List<Component> buildChatLines(Animal animal) {
        if (AnimalWeightsRules.isDisabled(animal)) {
            return List.of();
        }
        if (!AnimalWeightsRules.isActive(animal)) {
            return List.of(nameLine(animal), wildLine());
        }
        Snapshot s = snapshot(animal, -1, HabitatScanner.hasUsableFeedingTrough(animal));
        List<Component> lines = new ArrayList<>(5);
        lines.add(nameLine(animal));
        lines.add(weightLine(s));
        if (s.sick) {
            lines.add(sickLine());
        }
        lines.add(checksLine(s));
        lines.add(nextLine(s));
        return lines;
    }

    public static List<Component> buildCompactLines(Animal animal, int elapsedTicksOverride, boolean wild, boolean troughFood) {
        if (AnimalWeightsRules.isDisabled(animal)) {
            return List.of();
        }
        if (wild) {
            return List.of(wildLine());
        }
        Snapshot s = snapshot(animal, elapsedTicksOverride, troughFood);
        List<Component> lines = new ArrayList<>(4);
        lines.add(weightLine(s));
        if (s.sick) {
            lines.add(sickLine());
        }
        lines.add(checksLine(s));
        lines.add(nextLine(s));
        return lines;
    }

    private static MutableComponent nameLine(Animal animal) {
        return animal.getType().getDescription().copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    private static MutableComponent wildLine() {
        return Component.translatable("animalweights.inspect.wild").withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent sickLine() {
        return Component.translatable("animalweights.inspect.sick").withStyle(ChatFormatting.RED);
    }

    private static MutableComponent weightLine(Snapshot s) {
        return Component.translatable("animalweights.inspect.weight",
            Component.literal(s.weight + "/" + s.maxWeight).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent check(String need, boolean met) {
        return Component.translatable("animalweights.inspect." + need).withStyle(ChatFormatting.GRAY)
            .append(" ")
            .append(met ? CHECK : CROSS);
    }

    private static MutableComponent checksLine(Snapshot s) {
        MutableComponent line = check("light", s.light);
        if (s.diet == Diet.NETHER) {
            line.append(SEPARATOR).append(check("lava", s.lava))
                .append(SEPARATOR).append(check("nylium", s.netherGround));
        } else if (s.diet == Diet.AQUATIC) {
            line.append(SEPARATOR).append(check("water", s.water));
        } else {
            line.append(SEPARATOR).append(check("water", s.water))
                .append(SEPARATOR).append(check("grazing", s.grazing));
        }
        return line.append(SEPARATOR).append(check("space", s.notCrowded));
    }

    private static MutableComponent nextLine(Snapshot s) {
        if (s.outOfElement) {
            return Component.translatable(s.diet == Diet.NETHER ? "animalweights.inspect.waiting_for_nether" : "animalweights.inspect.paused_in_nether")
                .withStyle(ChatFormatting.DARK_AQUA);
        }
        if (s.sleeping) {
            return Component.translatable("animalweights.inspect.resting")
                .withStyle(ChatFormatting.DARK_AQUA);
        }
        return Component.translatable("animalweights.inspect.next",
            Component.translatable("animalweights.inspect.seconds", s.secondsUntilNext).withStyle(ChatFormatting.WHITE),
            s.outcome
        ).withStyle(ChatFormatting.GRAY);
    }

    private static Snapshot snapshot(Animal animal, int elapsedTicksOverride, boolean troughFood) {
        AnimalWeightsConfig cfg = ConfigManager.get();
        Level level = animal.level();
        BlockPos pos = animal.blockPosition();
        Diet diet = AnimalWeightsRules.dietOf(animal);
        WeightStats stats = AnimalWeightsRules.statsOf(animal);
        Snapshot s = new Snapshot();
        s.diet = diet;
        s.weight = WeightAttachment.getWeight(animal);
        s.maxWeight = stats.maxWeight();
        s.sick = stats.isSick(s.weight);
        s.light = HabitatScanner.hasBrightLight(level, pos);
        s.notCrowded = WeightTickLogic.hasSpace(animal, level, cfg);
        s.water = HabitatScanner.hasUsableNaturalWater(level, pos, diet)
            || HabitatScanner.findUsableCauldron(level, pos, diet) != null;
        s.grazing = HabitatScanner.hasGrazingNearby(level, pos, cfg.habitatScanRadius) || troughFood;
        s.lava = diet == Diet.NETHER && HabitatScanner.hasLavaNearby(level, pos, cfg.habitatScanRadius);
        s.netherGround = diet == Diet.NETHER && HabitatScanner.hasNetherGroundNearby(level, pos, cfg.habitatScanRadius);
        s.outOfElement = WeightTickLogic.isOutOfElement(level, diet);
        int score = WeightTickLogic.scoreHabitat(animal, level, cfg, diet, s.water, s.grazing);
        int interval = Math.max(1, stats.weightTickIntervalTicks());
        int elapsed = elapsedTicksOverride >= 0 ? elapsedTicksOverride : WeightAttachment.get(animal).getTicksSinceEvaluation();
        int ticksUntilNext = Math.max(0, interval - elapsed);
        s.secondsUntilNext = ticksUntilNext / 20L;
        s.outcome = predictOutcome(score);
        s.sleeping = AnimalWeightsRules.isSleeping(level);
        return s;
    }

    private static Component predictOutcome(int score) {
        if (score >= 4) {
            return Component.translatable("animalweights.inspect.outcome.gain").withStyle(ChatFormatting.GREEN);
        }
        if (score == 3) {
            return Component.translatable("animalweights.inspect.outcome.stable").withStyle(ChatFormatting.WHITE);
        }
        if (score == 2) {
            return Component.translatable("animalweights.inspect.outcome.minor_loss").withStyle(ChatFormatting.GOLD);
        }
        return Component.translatable("animalweights.inspect.outcome.severe_loss").withStyle(ChatFormatting.RED);
    }

    private static final class Snapshot {
        Diet diet;
        int weight;
        int maxWeight;
        boolean sick;
        boolean light;
        boolean water;
        boolean grazing;
        boolean lava;
        boolean netherGround;
        boolean notCrowded;
        boolean sleeping;
        boolean outOfElement;
        long secondsUntilNext;
        Component outcome;
    }
}

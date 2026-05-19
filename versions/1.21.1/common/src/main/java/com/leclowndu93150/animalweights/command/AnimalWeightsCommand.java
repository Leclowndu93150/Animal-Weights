package com.leclowndu93150.animalweights.command;

import com.leclowndu93150.animalweights.WeightAttachment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

public final class AnimalWeightsCommand {
    private static final double REACH = 32.0;

    private AnimalWeightsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("animalweights")
            .requires(src -> src.hasPermission(2));

        root.executes(ctx -> info(ctx.getSource(), lookingAtList(ctx.getSource())));
        root.then(Commands.literal("info")
            .executes(ctx -> info(ctx.getSource(), lookingAtList(ctx.getSource())))
            .then(Commands.argument("targets", EntityArgument.entities())
                .executes(ctx -> info(ctx.getSource(), EntityArgument.getEntities(ctx, "targets")))));
        root.then(Commands.literal("set")
            .then(Commands.argument("weight", IntegerArgumentType.integer(0, 8))
                .executes(ctx -> set(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "weight"), lookingAtList(ctx.getSource())))
                .then(Commands.argument("targets", EntityArgument.entities())
                    .executes(ctx -> set(
                        ctx.getSource(),
                        IntegerArgumentType.getInteger(ctx, "weight"),
                        EntityArgument.getEntities(ctx, "targets"))))));

        dispatcher.register(root);
    }

    private static int info(CommandSourceStack source, Collection<? extends Entity> targets) {
        if (targets.isEmpty()) {
            source.sendFailure(Component.literal("No targets"));
            return 0;
        }
        int matched = 0;
        for (Entity target : targets) {
            source.sendSuccess(() -> Component.literal("Target: ")
                .append(target.getType().getDescription())
                .withStyle(ChatFormatting.AQUA), false);
            source.sendSuccess(() -> Component.literal("  class: " + target.getClass().getSimpleName()), false);
            boolean isAnimal = target instanceof Animal;
            source.sendSuccess(() -> Component.literal("  is Animal: " + isAnimal)
                .withStyle(isAnimal ? ChatFormatting.GREEN : ChatFormatting.RED), false);
            if (target instanceof Animal animal) {
                matched++;
                int weight = WeightAttachment.getWeight(animal);
                source.sendSuccess(() -> Component.literal("  weight: " + weight).withStyle(ChatFormatting.YELLOW), false);
            }
        }
        return matched;
    }

    private static int set(CommandSourceStack source, int weight, Collection<? extends Entity> targets) {
        int changed = 0;
        for (Entity target : targets) {
            if (target instanceof Animal animal) {
                WeightAttachment.setWeight(animal, weight);
                changed++;
            }
        }
        int finalChanged = changed;
        if (changed == 0) {
            source.sendFailure(Component.literal("No Animal targets"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Set weight to " + weight + " on " + finalChanged + " entit" + (finalChanged == 1 ? "y" : "ies"))
            .withStyle(ChatFormatting.GREEN), false);
        return changed;
    }

    private static List<Entity> lookingAtList(CommandSourceStack source) {
        Entity e = requireLookingAt(source);
        return e == null ? List.of() : List.of(e);
    }

    private static Entity requireLookingAt(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return null;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(REACH));
        AABB scan = player.getBoundingBox().expandTowards(look.scale(REACH)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player, eye, end, scan,
            e -> e instanceof LivingEntity && e != player && e.isAlive(),
            REACH * REACH
        );
        return hit == null ? null : hit.getEntity();
    }
}

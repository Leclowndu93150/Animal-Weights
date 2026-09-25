package com.leclowndu93150.animalweights.display;

import com.leclowndu93150.animalweights.config.AnimalWeightsConfig;
import com.leclowndu93150.animalweights.config.ConfigManager;
import com.leclowndu93150.animalweights.config.OverlayMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class OverlayVisibility {
    private OverlayVisibility() {
    }

    public static boolean shouldRender(Animal animal, LocalPlayer player) {
        if (player == null) return false;
        if (!holdsRequiredItem(player)) return false;
        AnimalWeightsConfig cfg = ConfigManager.get();
        OverlayMode mode = cfg.overlayMode;
        if (mode == OverlayMode.ALWAYS) {
            return true;
        }
        double range = cfg.overlayRange;
        double rangeSq = range * range;
        if (player.distanceToSqr(animal) > rangeSq) {
            return false;
        }
        return switch (mode) {
            case NEARBY -> true;
            case LOOKING -> isLookingAt(player, animal, range);
            case CROUCH_LOOKING -> player.isCrouching() && isLookingAt(player, animal, range);
            default -> true;
        };
    }

    public static boolean holdsRequiredItem(Player player) {
        Set<String> required = ConfigManager.get().overlayRequiredItems;
        if (required.isEmpty()) {
            return true;
        }
        return player != null && (required.contains(itemId(player.getMainHandItem())) || required.contains(itemId(player.getOffhandItem())));
    }

    private static String itemId(ItemStack stack) {
        return String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isLookingAt(LocalPlayer player, Animal target, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB scan = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player, eye, end, scan,
            e -> e == target,
            range * range
        );
        return hit != null && hit.getEntity() == target;
    }
}

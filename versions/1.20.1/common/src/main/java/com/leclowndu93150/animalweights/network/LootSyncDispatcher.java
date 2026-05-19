package com.leclowndu93150.animalweights.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class LootSyncDispatcher {
    private static EntryBroadcaster broadcaster = (type, items) -> {};
    private static SnapshotSender snapshotSender = (player) -> {};

    private LootSyncDispatcher() {
    }

    public static void installBroadcaster(EntryBroadcaster impl) {
        broadcaster = impl;
    }

    public static void installSnapshot(SnapshotSender impl) {
        snapshotSender = impl;
    }

    public static void broadcastEntry(ResourceLocation type, List<ResourceLocation> items) {
        broadcaster.send(type, items);
    }

    public static void sendSnapshot(ServerPlayer player) {
        snapshotSender.send(player);
    }

    public interface EntryBroadcaster {
        void send(ResourceLocation type, List<ResourceLocation> items);
    }

    public interface SnapshotSender {
        void send(ServerPlayer player);
    }
}

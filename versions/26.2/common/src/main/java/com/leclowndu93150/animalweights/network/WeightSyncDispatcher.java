package com.leclowndu93150.animalweights.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public final class WeightSyncDispatcher {
    private static Sender sender = (animal) -> {};
    private static PlayerSender playerSender = (player, animal) -> {};

    private WeightSyncDispatcher() {
    }

    public static void install(Sender impl) {
        sender = impl;
    }

    public static void installPlayer(PlayerSender impl) {
        playerSender = impl;
    }

    public static void broadcast(Animal animal) {
        sender.send(animal);
    }

    public static void sendTo(ServerPlayer player, Animal animal) {
        playerSender.send(player, animal);
    }

    public interface Sender {
        void send(Animal animal);
    }

    public interface PlayerSender {
        void send(ServerPlayer player, Animal animal);
    }
}

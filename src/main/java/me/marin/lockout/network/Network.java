package me.marin.lockout.network;

import me.marin.lockout.server.LockoutServer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class Network {
    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcast(CustomPacketPayload payload) {
        LockoutServer.server.getPlayerList().getPlayers().forEach( p ->
                send(p, payload)
        );
    }

    public static void broadcastPlayers(CustomPacketPayload payload) {
        if(LockoutServer.lockout == null) return;
        LockoutServer.server.getPlayerList().getPlayers()
                .stream().filter(p -> LockoutServer.lockout.isLockoutPlayer(p))
                .forEach(p -> send(p, payload));
    }

    public static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}

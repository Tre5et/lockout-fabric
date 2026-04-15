package me.marin.lockout.server.handlers;

import me.marin.lockout.LockoutInitializer;
import me.marin.lockout.network.LockoutVersionPayload;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import java.util.UUID;

import static me.marin.lockout.server.LockoutServer.waitingForVersionPacketPlayersMap;

public class PlayerJoinEventHandler implements ServerPlayConnectionEvents.Join {
    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        // Check if the client has the correct mod version:
        // 1. Send the Lockout version packet
        // 2. Store timestamp in waiting map
        // 3. If version response arrives within timeout, validate version
        // 4. If timeout expires, kick player for missing mod

        ServerPlayer player = handler.getPlayer();

        ServerPlayNetworking.send(player, new LockoutVersionPayload(LockoutInitializer.MOD_VERSION.getFriendlyString()));

        waitingForVersionPacketPlayersMap.put(player.getUUID(), System.currentTimeMillis());
    }
}

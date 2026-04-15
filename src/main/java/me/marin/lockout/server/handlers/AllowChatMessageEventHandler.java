package me.marin.lockout.server.handlers;

import me.marin.lockout.ChatManager;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeamServer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;

import static me.marin.lockout.server.LockoutServer.lockout;

public class AllowChatMessageEventHandler implements ServerMessageEvents.AllowChatMessage {

    @Override
    public boolean allowChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound parameters) {
        if (ChatManager.getChat(sender) == ChatManager.Type.TEAM) {
            String m = "[Team Chat] " + ChatFormatting.RESET + "<" + sender.getName().getString() + "> " + message.decoratedContent().getString();
            if (Lockout.isLockoutRunning(lockout)) {
                LockoutTeamServer team = (LockoutTeamServer) lockout.getPlayerTeam(sender.getUUID());
                team.sendMessage(team.getColor() + m);
            } else {
                PlayerTeam team = sender.getTeam();
                if (team == null) {
                    return true;
                }
                MinecraftServer server = sender.level().getServer();
                PlayerList pm = server.getPlayerList();

                team.getPlayers().stream().filter(p -> pm.getPlayerByName(p) != null).map(pm::getPlayerByName).forEach(p ->{
                    p.sendSystemMessage(Component.literal(team.getColor() + m));
                });
            }
            return false;
        }
        return true;
    }
}

package me.marin.lockout.game;

import lombok.Getter;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public enum GameState {
    STARTING(false, true, false, GameType.ADVENTURE, GameType.SPECTATOR),
    RUNNING(true, true, true, GameType.SURVIVAL, GameType.SPECTATOR),
    PAUSED(false, false, true, GameType.ADVENTURE, GameType.SPECTATOR),
    FINISHED(false, false, false, GameType.SPECTATOR, GameType.SPECTATOR);

    @Getter
    private final boolean shouldTick;
    @Getter
    private final boolean shouldTrackTime;
    @Getter
    private final boolean shouldSave;
    @Getter @Nullable
    private final GameType playerGameType;
    @Getter @Nullable
    private final GameType spectatorGameType;

    GameState(boolean shouldTick, boolean shouldTrackTime, boolean shouldSave, @Nullable GameType playerGameType, @Nullable GameType spectatorGameType) {
        this.shouldTick = shouldTick;
        this.shouldTrackTime = shouldTrackTime;
        this.shouldSave = shouldSave;
        this.playerGameType = playerGameType;
        this.spectatorGameType = spectatorGameType;
    }

    public void applyServer(MinecraftServer server, ServerLockoutGame game, boolean broadcast) {
        server.tickRateManager().setFrozen(!shouldTick);
        server.getPlayerList().getPlayers().forEach(player -> {
            applyPlayer(player, game, broadcast);
        });
    }

    public void applyPlayer(ServerPlayer player, ServerLockoutGame game, boolean broadcast) {
        if (game.isLockoutPlayer(player.getUUID())) {
            if(playerGameType != null) player.setGameMode(playerGameType);
        } else {
            if(spectatorGameType != null) player.setGameMode(spectatorGameType);
            if(broadcast) player.sendSystemMessage(Component.literal("You are spectating this match.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}

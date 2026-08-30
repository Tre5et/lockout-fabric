package me.marin.lockout.server.handlers;

import me.marin.lockout.LockoutRunnable;
import me.marin.lockout.game.GameState;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;

import static me.marin.lockout.server.LockoutServer.gameStartRunnables;
import static me.marin.lockout.server.LockoutServer.lockout;

public class EndServerTickEventHandler implements ServerTickEvents.EndTick {

    @Override
    public void onEndTick(@NonNull MinecraftServer server) {
        if (lockout == null) return;

        for (LockoutRunnable runnable : new HashSet<>(gameStartRunnables.keySet())) {
            if (gameStartRunnables.get(runnable) <= 0) {
                runnable.run();
                gameStartRunnables.remove(runnable);
            } else {
                gameStartRunnables.merge(runnable, -1L, Long::sum);
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            lockout.getBoard().update(player.getInventory(), player);
            if (player.isPassenger()) {
                lockout.getBoard().update(new EntityUtil.RodeEntity(player.getVehicle().getType()), player);
            }
        }

        if(lockout.getState() != GameState.FINISHED) {
            lockout.tick();
            if (lockout.getTicks() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());
                }
            }
        }
    }
}

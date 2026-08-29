package me.marin.lockout.server.goal.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.goal.progress.GoalProgress;
import me.marin.lockout.network.GoalProgressPayload;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface ServerGoalProgress<U,T> extends GoalProgress<T> {
    Gson GSON = new GsonBuilder().create();

    T update(T current, U update);

    default void grant(LockoutTeam team, ServerLockoutGame lockout) {
        int teamIndex = lockout.getTeams().indexOf(team);
        getProgress().put(teamIndex, getCompletedProgress());
    }

    default boolean update(LockoutTeam team, U update, ServerLockoutGame lockout) {
        int teamIndex = lockout.getTeams().indexOf(team);
        T previousProgress = getProgress(teamIndex);
        T newProgress = update(previousProgress, update);
        if(previousProgress.equals(newProgress)) return false;
        getProgress().put(teamIndex, newProgress);
        return true;
    }

    default void reset(LockoutTeam team, ServerLockoutGame lockout) {
        int teamIndex = lockout.getTeams().indexOf(team);
        getProgress().remove(teamIndex);
    }

    default void resetAll() {
        getProgress().clear();
    }

    default void send(String goalId, List<ServerPlayer> players) {
        GoalProgressPayload payload = new GoalProgressPayload(goalId, GSON.toJson(serialize()));
        players.forEach(p -> ServerPlayNetworking.send(p, payload));
    }
}

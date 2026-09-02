package me.marin.lockout.server.goal.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.goal.progress.GoalProgress;
import me.marin.lockout.network.GoalProgressPayload;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface ServerGoalProgress<U,T> extends GoalProgress<T> {
    Gson GSON = new GsonBuilder().create();

    T update(T current, U update);

    default <M> MappedServerGoalProgress<U,T,M> map(Function<M,U> mapper) {
        return new MappedServerGoalProgress<>(this, mapper);
    }

    class MappedServerGoalProgress<U,T,M> implements ServerGoalProgress<M,T> {
        private final ServerGoalProgress<U,T> original;
        private final Function<M,U> mapper;

        public MappedServerGoalProgress(ServerGoalProgress<U, T> original, Function<M, U> mapper) {
            this.original = original;
            this.mapper = mapper;
        }

        @Override
        public T update(T current, M update) {
            return original.update(current, mapper.apply(update));
        }

        @Override
        public Map<Integer, T> getProgress() {
            return original.getProgress();
        }

        @Override
        public T getDefaultProgress() {
            return original.getDefaultProgress();
        }

        @Override
        public T getCompletedProgress() {
            return original.getCompletedProgress();
        }

        @Override
        public boolean isCompleted(T value) {
            return original.isCompleted(value);
        }

        @Override
        public JsonElement serializeData(T value) {
            return original.serializeData(value);
        }

        @Override
        public T deserializeData(JsonElement element) throws IllegalArgumentException {
            return original.deserializeData(element);
        }
    }

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

    default void send(String goalId, List<ServerPlayer> players, Optional<LockoutTeam> newCompletion, ServerLockoutGame lockout) {
        GoalProgressPayload payload = new GoalProgressPayload(goalId, GSON.toJson(serialize()), newCompletion.map(t -> lockout.getTeams().indexOf(t)));
        players.forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    default void send(String goalId, List<ServerPlayer> players) {
        send(goalId, players, Optional.empty(), null);
    }
}

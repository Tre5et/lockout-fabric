package me.marin.lockout.server.goal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import me.marin.lockout.server.goal.builder.ServerGoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerGoal<U> extends Goal {
    @Getter
    private final ServerGoalProgress<U,?> progress;
    @Getter
    private final List<ServerHint<?>> hints;

    public ServerGoal(ServerGoalBuildParameters<U> parameters) {
        super(parameters);
        this.progress = parameters.getProgress();
        this.hints = parameters.getHints();
    }

    public void updateProgress(LockoutTeam team, U update, ServerLockoutGame lockout) {
        handlingProgress(team, lockout, () -> progress.update(team, update, lockout));
    }

    @SuppressWarnings("unchecked")
    public void updateProgressUnchecked(LockoutTeam team, Object update, ServerLockoutGame lockout) {
        try {
            updateProgress(team, (U)update, lockout);
        } catch (ClassCastException ignored) {}
    }

    public void grant(LockoutTeam team, ServerLockoutGame lockout) {
        handlingProgress(team, lockout, () -> {
            getProgress().grant(team, lockout);
            return true;
        });
    }

    public void revoke(LockoutTeam team, ServerLockoutGame lockout) {
        handlingProgress(team, lockout, () -> {
            getProgress().reset(team, lockout);
            return true;
        });
    }

    public void handlingProgress(LockoutTeam team, ServerLockoutGame lockout, Supplier<Boolean> updater) {
        boolean prevCompleted = getProgress().isCompleted(team, lockout);
        if(updater.get()) {
            Optional<LockoutTeam> completedTeam = (!prevCompleted && getProgress().isCompleted(team, lockout)) ? Optional.of(team) : Optional.empty();
            getProgress().send(getId(), LockoutServer.server.getPlayerList().getPlayers(), completedTeam, lockout);
        }
    }

    public static ServerGoal<?> deserialize(JsonElement element) throws IllegalGoalConstructionException {
        if (element == null || !element.isJsonObject())
            throw new IllegalGoalConstructionException("Server goal data is not an object.");
        JsonObject data = element.getAsJsonObject();

        if (!data.has("id") || !data.get("id").isJsonPrimitive() || !data.get("id").getAsJsonPrimitive().isString()) throw new IllegalGoalConstructionException("Server goal data does not contain a valid id.");
        if (!GoalRegistry.INSTANCE.isRegistered(data.get("id").getAsString())) throw new IllegalGoalConstructionException("Goal with id " + data.get("id").getAsString() + " is not registered.");
        GoalBuilder<?,?> builder = GoalRegistry.INSTANCE.get(data.get("id").getAsString());

        String option = null;
        if(data.has("option")) {
            JsonElement optionElement = data.get("option");
            if(!optionElement.isJsonPrimitive() || !optionElement.getAsJsonPrimitive().isString()) throw new IllegalGoalConstructionException("Server goal data option is invalid.");
            option = optionElement.getAsString();
        }
        ServerGoal<?> goal = builder.buildServerFromSerializedData(option);
        if(data.has("progress")) {
            try {
                goal.getProgress().deserialize(data.get("progress"));
            } catch (IllegalArgumentException e) {
                throw new IllegalGoalConstructionException("Failed to deserialize goal progress", e);
            }
        }
        return goal;
    }

    public static List<ServerGoal<?>> constructAll(List<BuildData> data) throws IllegalGoalConstructionException {
        List<ServerGoal<?>> goals = new ArrayList<>();
        List<Pair<BuildData, Exception>> invalidGoals = new ArrayList<>();
        for(BuildData goal : data) {
            if(!GoalRegistry.INSTANCE.isRegistered(goal.id())) {
                invalidGoals.add(new Pair<>(goal, new IllegalGoalConstructionException("Goal does not exist")));
            } else {
                try {
                    goals.add(GoalRegistry.INSTANCE.get(goal.id()).buildServerFromSerializedData(goal.option().orElse(null)));
                } catch (IllegalGoalConstructionException e) {
                    invalidGoals.add(new Pair<>(goal, e));
                }
            }
        }
        if(!invalidGoals.isEmpty()) {
            throw new IllegalGoalConstructionException("Failed to construct some goals: " +
                    invalidGoals.stream()
                            .map(g -> g.getA().id() + " (" + g.getA().option().orElse("null") + "): " + g.getB().getMessage())
                            .collect(Collectors.joining("; "))
            );
        }
        return goals;
    }
}

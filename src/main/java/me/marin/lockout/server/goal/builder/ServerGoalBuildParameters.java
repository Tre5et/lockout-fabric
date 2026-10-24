package me.marin.lockout.server.goal.builder;

import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;

import java.util.List;
import java.util.function.Consumer;

public class ServerGoalBuildParameters<U> extends GoalBuildParameters {
    @Getter
    private final ServerGoalProgress<U,?> progress;
    @Getter
    private final List<ServerHint<?>> hints;
    @Getter
    private final Consumer<U> reifiedUpdater;
    @Getter
    private final float difficulty;

    public ServerGoalBuildParameters(String id, BuildData buildData, ServerGoalProgress<U, ?> progress, List<ServerHint<?>> hints, Consumer<U> reifiedUpdater, float difficulty) {
        super(id, buildData);
        this.progress = progress;
        this.hints = hints;
        this.reifiedUpdater = reifiedUpdater;
        this.difficulty = difficulty;
    }
}

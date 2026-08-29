package me.marin.lockout.server.goal.builder;

import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;

import java.util.List;

public class ServerGoalBuildParameters<U> extends GoalBuildParameters {
    @Getter
    private final ServerGoalProgress<U,?> progress;
    @Getter
    private final List<ServerHint<?>> hints;

    public ServerGoalBuildParameters(String id, BuildData buildData, ServerGoalProgress<U, ?> progress, List<ServerHint<?>> hints) {
        super(id, buildData);
        this.progress = progress;
        this.hints = hints;
    }
}

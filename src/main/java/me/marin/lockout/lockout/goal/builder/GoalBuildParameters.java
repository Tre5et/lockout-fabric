package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;

public class GoalBuildParameters {
    @Getter
    private final BuildData buildData;

    public GoalBuildParameters(BuildData buildData) {
        this.buildData = buildData;
    }
}

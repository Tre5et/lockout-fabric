package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;

public class GoalBuildParameters {
    @Getter
    private final String id;
    @Getter
    private final BuildData buildData;

    public GoalBuildParameters(String id, BuildData buildData) {
        this.id = id;
        this.buildData = buildData;
    }
}

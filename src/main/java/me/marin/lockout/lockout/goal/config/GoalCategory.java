package me.marin.lockout.lockout.goal.config;

import lombok.Getter;

public enum GoalCategory {
    TOOLS("Tools"),
    OBTAIN("Obtain"),
    RIDE("Ride");

    @Getter
    private final String name;

    GoalCategory(String name) {
        this.name = name;
    }
}

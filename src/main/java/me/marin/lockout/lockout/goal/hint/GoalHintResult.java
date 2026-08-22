package me.marin.lockout.lockout.goal.hint;

import lombok.Getter;

public class GoalHintResult {
    @Getter
    private final String message;
    @Getter
    private final boolean success;

    public GoalHintResult(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public static GoalHintResult result(String message) {
        return new GoalHintResult(message, true);
    }

    public static GoalHintResult error(String message) {
        return new GoalHintResult(message, false);
    }
}

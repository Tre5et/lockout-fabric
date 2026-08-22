package me.marin.lockout.lockout.goal.builder;

public class IllegalGoalConstructionException extends Exception {
    public IllegalGoalConstructionException(String message) {
        super(message);
    }

    public IllegalGoalConstructionException(String message, Throwable cause) {
        super(message, cause);
    }
}

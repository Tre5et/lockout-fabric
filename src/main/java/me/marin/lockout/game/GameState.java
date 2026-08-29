package me.marin.lockout.game;

import lombok.Getter;

public enum GameState {
    STARTING(false),
    RUNNING(true),
    FINISHED(false);

    @Getter
    private final boolean isActive;

    GameState(boolean isActive) {
        this.isActive = isActive;
    }
}

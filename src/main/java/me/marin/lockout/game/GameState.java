package me.marin.lockout.game;

import lombok.Getter;

public enum GameState {
    STARTING(false, true, false),
    RUNNING(true, true, true),
    PAUSED(false, false, true),
    FINISHED(false, false, true);

    @Getter
    private final boolean isActive;
    @Getter
    private final boolean shouldTick;
    @Getter
    private final boolean shouldSave;

    GameState(boolean isActive, boolean shouldTick, boolean shouldSave) {
        this.isActive = isActive;
        this.shouldTick = shouldTick;
        this.shouldSave = shouldSave;
    }
}

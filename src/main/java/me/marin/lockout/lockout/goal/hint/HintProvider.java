package me.marin.lockout.lockout.goal.hint;

public interface HintProvider<T extends Hint<H>, H> {
    T provide();
}

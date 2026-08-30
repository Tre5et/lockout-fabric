package me.marin.lockout.lockout.goal.rendering.id;

public interface IdProvider<T> {
    String get(T option);
}

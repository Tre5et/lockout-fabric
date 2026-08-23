package me.marin.lockout.lockout.goal.id;

public interface IdProvider<T> {
    String get(T option);
}

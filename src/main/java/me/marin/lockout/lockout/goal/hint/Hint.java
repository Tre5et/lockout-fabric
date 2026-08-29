package me.marin.lockout.lockout.goal.hint;

import com.google.gson.JsonElement;

public interface Hint<T> {
    T deserialize(JsonElement data) throws IllegalArgumentException;
    JsonElement serialize(T data);
}

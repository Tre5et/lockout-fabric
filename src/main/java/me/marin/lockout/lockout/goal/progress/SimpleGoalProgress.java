package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class SimpleGoalProgress implements GoalProgress<Boolean> {
    private final Map<Integer, Boolean> progress = new HashMap<>();

    @Override
    public Map<Integer, Boolean> getProgress() {
        return progress;
    }

    @Override
    public Boolean getDefaultProgress() {
        return false;
    }

    @Override
    public Boolean getCompletedProgress() {
        return true;
    }

    @Override
    public boolean isCompleted(Boolean value) {
        return value;
    }

    @Override
    public JsonElement serializeData(Boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Boolean deserializeData(JsonElement element) throws IllegalArgumentException {
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("Boolean progress data is not a boolean.");
        return element.getAsBoolean();
    }
}

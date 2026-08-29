package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TargetNumberGoalProgress implements GoalProgress<Integer> {
    private final Map<Integer, Integer> progress = new HashMap<>();

    @Getter
    private final int target;

    public TargetNumberGoalProgress(int target) {
        this.target = target;
    }

    @Override
    public Map<Integer, Integer> getProgress() {
        return progress;
    }

    @Override
    public Integer getDefaultProgress() {
        return 0;
    }

    @Override
    public Integer getCompletedProgress() {
        return target;
    }

    @Override
    public boolean isCompleted(Integer value) {
        return value >= target;
    }

    @Override
    public JsonElement serializeData(Integer value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Integer deserializeData(JsonElement element) throws IllegalArgumentException {
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Target progress is not a number.");
        return element.getAsInt();
    }
}

package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TargetFloatGoalProgress implements GoalProgress<Number> {
    private final Map<Integer, Number> progress = new HashMap<>();

    @Getter
    private final Number target;

    public TargetFloatGoalProgress(Number target) {
        this.target = target;
    }

    @Override
    public Map<Integer, Number> getProgress() {
        return progress;
    }

    @Override
    public Number getDefaultProgress() {
        return 0;
    }

    @Override
    public Number getCompletedProgress() {
        return target;
    }

    @Override
    public boolean isCompleted(Number value) {
        return value.doubleValue() > target.doubleValue();
    }

    @Override
    public JsonElement serializeData(Number value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Number deserializeData(JsonElement element) throws IllegalArgumentException {
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Target progress is not a number.");
        return element.getAsNumber();
    }
}

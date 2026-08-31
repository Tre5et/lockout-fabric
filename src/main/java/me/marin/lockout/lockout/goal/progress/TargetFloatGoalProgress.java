package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TargetFloatGoalProgress implements GoalProgress<Float> {
    private final Map<Integer, Float> progress = new HashMap<>();

    @Getter
    private final Float target;

    public TargetFloatGoalProgress(Float target) {
        this.target = target;
    }

    @Override
    public Map<Integer, Float> getProgress() {
        return progress;
    }

    @Override
    public Float getDefaultProgress() {
        return 0f;
    }

    @Override
    public Float getCompletedProgress() {
        return target;
    }

    @Override
    public boolean isCompleted(Float value) {
        return value >= target;
    }

    @Override
    public JsonElement serializeData(Float value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Float deserializeData(JsonElement element) throws IllegalArgumentException {
        if(element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Target progress is not a number.");
        return element.getAsFloat();
    }
}

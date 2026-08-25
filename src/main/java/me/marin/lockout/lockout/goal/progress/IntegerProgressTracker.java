package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;

public class IntegerProgressTracker extends GoalProgressTracker<Integer, Integer> {
    public IntegerProgressTracker(Component title) {
        super(title);
    }

    @Override
    protected String displayString(Integer value) {
        return String.valueOf(value);
    }

    @Override
    protected Integer defaultValue() {
        return 0;
    }

    @Override
    protected Integer deserialize(JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsInt();
    }

    @Override
    protected JsonElement serialize(Integer progress) {
        return new JsonPrimitive(progress == null ? 0 : progress);
    }

    @Override
    protected Integer updateProgress(Integer oldProgress, Integer newProgress) {
        return (oldProgress == null ? 0 : oldProgress) + (newProgress == null ? 0 : newProgress);
    }
}

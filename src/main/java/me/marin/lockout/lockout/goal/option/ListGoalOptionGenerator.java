package me.marin.lockout.lockout.goal.option;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class ListGoalOptionGenerator<T> implements GoalOptionGenerator<T> {
    @Getter
    private final List<T> entries;
    private final TypeToken<T> typeToken;

    public ListGoalOptionGenerator(List<T> entries, TypeToken<T> typeToken) {
        this.entries = entries;
        this.typeToken = typeToken;
    }

    @Override
    public Optional<T> generate(Function<T, Boolean> allowOption) {
        List<T> shuffled = new ArrayList<>(entries);
        Collections.shuffle(shuffled, RANDOM);
        return shuffled.stream()
                .filter(allowOption::apply)
                .findFirst();
    }

    @Override
    public List<T> examples() {
        return entries;
    }

    @Override
    public String serialize(T option) {
        return GSON.toJson(option);
    }

    @Override
    public T deserialize(String serialized) throws IllegalGoalConstructionException {
        try {
            return GSON.fromJson(serialized, typeToken);
        } catch (JsonSyntaxException e) {
            throw new IllegalGoalConstructionException("Failed to deserialize option: " + serialized, e);
        }
    }
}

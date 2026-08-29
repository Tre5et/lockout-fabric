package me.marin.lockout.lockout.goal.option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public interface GoalOptionGenerator<T> {
    Random RANDOM = new Random();
    Gson GSON = new GsonBuilder().create();

    Optional<T> generate(Function<T, Boolean> allowOption);
    List<T> examples();
    String serialize(T option);
    T deserialize(String serialized) throws IllegalGoalConstructionException;
}

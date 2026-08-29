package me.marin.lockout.lockout.goal.progress;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutGame;

import java.util.*;
import java.util.stream.Collectors;

public interface GoalProgress<T> {
    Map<Integer, T> getProgress();

    T getDefaultProgress();
    T getCompletedProgress();
    boolean isCompleted(T value);
    JsonElement serializeData(T value);
    T deserializeData(JsonElement element) throws IllegalArgumentException;

    default T getProgress(int teamIndex) {
        return getProgress().getOrDefault(teamIndex, getDefaultProgress());
    }

    default boolean isCompleted(int teamIndex) {
        return isCompleted(getProgress(teamIndex));
    }

    default boolean isCompleted(LockoutTeam team, LockoutGame<?> lockout) {
        return isCompleted(lockout.getTeams().indexOf(team));
    }

    default boolean hasAnyCompleted() {
        return getProgress().values().stream()
                .anyMatch(this::isCompleted);
    }

    default List<Integer> getCompletedTeams() {
        List<Integer> teams = new ArrayList<>();
        getProgress().forEach((k, v) -> {
            if(isCompleted(v)) teams.add(k);
        });
        return teams;
    }

    default List<LockoutTeam> getCompletedTeams(LockoutGame<?> lockout) {
        return getCompletedTeams().stream()
                .map(t -> lockout.getTeams().get(t))
                .collect(Collectors.toUnmodifiableList());
    }

    default JsonElement serialize() {
        JsonObject map = new JsonObject();
        getProgress().forEach((key, value) -> map.add(key.toString(), serializeData(value)));
        return map;
    }

    default void deserialize(JsonElement element) throws IllegalArgumentException {
        if(element == null || !element.isJsonObject()) throw new IllegalArgumentException("The data is not a JSON map.");
        JsonObject map = element.getAsJsonObject();

        Map<Integer, T> newProgress = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
            newProgress.put(Integer.valueOf(entry.getKey()), deserializeData(entry.getValue()));
        }
        getProgress().clear();
        getProgress().putAll(newProgress);
    }
}

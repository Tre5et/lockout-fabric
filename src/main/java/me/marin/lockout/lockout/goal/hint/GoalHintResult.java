package me.marin.lockout.lockout.goal.hint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.Getter;
import me.marin.lockout.network.HintResultPayload;

import java.util.Optional;
import java.util.function.Function;

public class GoalHintResult<T>{
    private static final Gson GSON = new GsonBuilder().create();

    @Getter
    private final T data;
    @Getter
    private final String error;

    public GoalHintResult(T data, String error) {
        this.data = data;
        this.error = error;
    }

    public HintResultPayload getPayload(String goalId, int hintIndex, Function<T, JsonElement> serializer) {
        String serializedData = data == null ? null : GSON.toJson(serializer.apply(data));
        return new HintResultPayload(goalId, hintIndex, Optional.ofNullable(serializedData), Optional.ofNullable(error));
    }

    public static <T> GoalHintResult<T> result(T data) {
        return new GoalHintResult<>(data, null);
    }

    public static <T> GoalHintResult<T> error(String message) {
        return new GoalHintResult<>(null, message);
    }

}

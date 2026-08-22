package me.marin.lockout.lockout.goal.option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GoalOptionGenerator<T> {
    Random RANDOM = new Random();
    Gson GSON = new GsonBuilder().create();

    Optional<T> generate(Function<T, Boolean> allowOption);
    List<T> examples();
    String serialize(T option);
    T deserialize(String serialized) throws IllegalGoalConstructionException;
    int getPreferredRenderWidth();
    int getPreferredRenderHeight();
    AbstractWidget getWidget(int x, int y, int width, int height, Font font, Consumer<T> update);

    static <T> ListGoalOptionGenerator<T> list(List<T> entries, TypeToken<T> typeToken) {
        return new ListGoalOptionGenerator<>(entries, typeToken);
    }
}

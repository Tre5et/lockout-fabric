package me.marin.lockout.lockout.goal.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeam;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GoalProgressTracker<T,R> {
    public static final Gson GSON = new GsonBuilder().create();

    private final Map<Integer, R> progress = new HashMap<>();

    private final Component title;

    public GoalProgressTracker(Component title) {
        this.title = title;
    }

    public Map<Integer, R> get() {
        return progress;
    }

    public R get(int team) {
        return progress.getOrDefault(team, defaultValue());
    }

    public void set(Map<Integer, R> newProgress) {
        progress.clear();
        progress.putAll(newProgress);
    }

    public void set(String serialized) {
        progress.clear();
        if (serialized == null || serialized.isBlank()) {
            return;
        }

        JsonElement root = JsonParser.parseString(serialized);
        if (!root.isJsonObject()) {
            return;
        }

        JsonObject data = root.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            try {
                progress.put(Integer.parseInt(entry.getKey()), deserialize(entry.getValue()));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid team ids when restoring a serialized progress tree.
            }
        }
    }

    public void update(Integer team, T newProgress) {
        progress.put(team, updateProgress(progress.getOrDefault(team, defaultValue()), newProgress));
    }

    public String serialize() {
        JsonObject data = new JsonObject();
        for(Map.Entry<Integer, R> entry : progress.entrySet()) {
            data.add(String.valueOf(entry.getKey()), serialize(entry.getValue()));
        }
        return GSON.toJson(data);
    }

    protected abstract String displayString(R value);
    protected abstract R defaultValue();

    protected abstract R deserialize(JsonElement e) throws IllegalArgumentException;
    protected abstract JsonElement serialize(R progress);
    protected abstract R updateProgress(R oldProgress, T newProgress);

    public List<Component> getTooltip(LockoutTeam team, Player player, Lockout lockout) {
        return List.of(title.copy().append(Component.literal(displayString(get(lockout.getTeams().indexOf(team))))));
    }
    public List<Component> getSpectatorTooltip(Lockout lockout) {
        List<Component> components = new ArrayList<>(List.of(title));
        for(int i = 0; i < lockout.getTeams().size(); i++) {
            components.add(Component.literal("- " + lockout.getTeams().get(i).getDisplayName() + ": " + displayString(get(i))));
        }
        return components;
    }

    public static Component constructTitle(String title) {
        return Component.literal("")
                .append(Component.literal(title).setStyle(Style.EMPTY.withUnderlined(true)))
                .append(Component.literal(": "));
    }

    public static IntegerProgressTracker integer(String title) {
        return new IntegerProgressTracker(constructTitle(title));
    }

    public static TargetIntegerProgressTracker integer(String title, int target) {
        return new TargetIntegerProgressTracker(constructTitle(title), target);
    }
}

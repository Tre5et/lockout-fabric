package me.marin.lockout.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.goal.Goal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.marin.lockout.Constants.MAX_BOARD_SIZE;
import static me.marin.lockout.Constants.MIN_BOARD_SIZE;

public class LockoutBoard {

    @Accessors(fluent = true)
    @Getter
    private final int size;

    private final List<Goal<?>> goals;

    public LockoutBoard(List<Goal<?>> goals) {
        size = (int) Math.sqrt(goals.size());
        if (goals.size() != size * size || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
            throw new IllegalArgumentException(String.format("Invalid number of goals (%d)", size));
        }
        this.goals = new ArrayList<>(goals);
    }

    public List<Goal<?>> getGoals() {
        return Collections.unmodifiableList(goals);
    }

    public JsonElement serialize(List<? extends LockoutTeam> teams) {
        JsonArray array = new JsonArray();
        goals.forEach(g -> array.add(g.serialize(teams, true)));
        return array;
    }

    public static LockoutBoard deserialize(JsonElement json, List<? extends LockoutTeam> teams) throws IOException {
        if(!json.isJsonArray()) throw new IOException("Board data does not contain valid goals.");

        List<Goal<?>> goals = new ArrayList<>();
        for(JsonElement element : json.getAsJsonArray()) {
            goals.add(Goal.deserialize(element, teams));
        }

        return new LockoutBoard(goals);
    }

}

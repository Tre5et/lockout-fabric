package me.marin.lockout.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.server.game.ServerLockoutBoard;
import me.marin.lockout.server.goal.ServerGoal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.marin.lockout.Constants.MAX_BOARD_SIZE;
import static me.marin.lockout.Constants.MIN_BOARD_SIZE;

public class LockoutBoard<T extends Goal> {
    @Getter
    private final List<T> goals;

    public LockoutBoard(List<T> goals) {
        int size = (int) Math.sqrt(goals.size());
        if (goals.size() != size * size || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
            throw new IllegalArgumentException(String.format("Invalid number of goals (%d)", size));
        }
        this.goals = new ArrayList<>(goals);
    }

    public int getSize() {
        return (int) Math.sqrt(goals.size());
    }

    public JsonElement serialize(List<? extends LockoutTeam> teams) {
        JsonArray array = new JsonArray();
        goals.forEach(g -> array.add(g.serialize(true)));
        return array;
    }

    public static ServerLockoutBoard deserialize(JsonElement json, List<? extends LockoutTeam> teams) throws IOException {
        if(!json.isJsonArray()) throw new IOException("Board data does not contain valid goals.");

        List<ServerGoal<?>> goals = new ArrayList<>();
        for(JsonElement element : json.getAsJsonArray()) {
            try {
                goals.add(ServerGoal.deserialize(element));
            } catch (IllegalGoalConstructionException e) {
                throw new IOException("Failed to construct a goal", e);
            }
        }

        return new ServerLockoutBoard(goals);
    }
}

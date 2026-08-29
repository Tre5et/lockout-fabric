package me.marin.lockout.lockout.goal;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.lockout.goal.progress.GoalProgress;

import java.util.List;

public abstract class Goal {
    @Getter
    private final String id;
    @Getter
    private final BuildData buildData;

    public Goal(GoalBuildParameters parameters) {
        this.id = parameters.getId();
        this.buildData = parameters.getBuildData();
    }

    public abstract GoalProgress<?> getProgress();

    public boolean hasAnyCompleted() {
        return getProgress().hasAnyCompleted();
    }

    public List<LockoutTeam> getCompletedTeams(LockoutGame<?> lockout) {
        return getProgress().getCompletedTeams(lockout);
    }

    public JsonElement serialize(boolean includeProgress) {
        JsonObject object = new JsonObject();
        object.add("id", new JsonPrimitive(buildData.id()));
        object.add("option", buildData.option().map(o -> (JsonElement)new JsonPrimitive(o)).orElse(JsonNull.INSTANCE));
        if(includeProgress) {
            object.add("progress", getProgress().serialize());
        }
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(!(obj instanceof Goal goal)) return false;
        return this.buildData.equals(goal.buildData);
    }

    @Override
    public int hashCode() {
        return buildData.hashCode();
    }
}

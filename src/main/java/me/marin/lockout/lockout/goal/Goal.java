package me.marin.lockout.lockout.goal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.network.CompleteTaskPayload;
import me.marin.lockout.server.LockoutServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Goal<T> {
    @Getter
    private final String id;
    @Getter
    private final NameExtractor nameExtractor;
    @Getter
    private final TextureExtractor textureExtractor;
    @Getter
    private boolean isCompleted = false;
    @Getter
    private LockoutTeam completedTeam;
    @Getter
    private final List<GoalHint> hints;

    @Getter
    // TODO: improve build data serialization
    private final Pair<String, String> buildData;

    public Goal(GoalBuildParameters parameters) {
        this.id = parameters.id();
        this.nameExtractor = parameters.nameExtractor();
        this.textureExtractor = parameters.textureExtractor();
        this.hints = parameters.hints();
        this.buildData = parameters.buildData();
    }

    public void setCompleted(boolean isCompleted, LockoutTeam team) {
        this.isCompleted = isCompleted;
        this.completedTeam = team;
    }

    public final Component extractName() {
        return nameExtractor.extract();
    }

    public final void extractTexture(GuiGraphicsExtractor extractor, Font font, int x, int y) {
        textureExtractor.extract(extractor, font, x, y, 16, 16, LockoutClient.CURRENT_TICK);
    }

    public List<Component> getTooltip(LockoutTeam team, Player player, Lockout lockout) {
        return List.of();
    }

    public List<Component> getSpectatorTooltip(Lockout lockout) {
        return List.of();
    }

    public String getProgress() {
        return null;
    }

    public void setProgress(String progress) {}

    public void sendProgress(ServerPlayer player) {}

    public abstract void updateWith(T data, ServerPlayer player);

    @SuppressWarnings("unchecked")
    public void updateIfValid(Object data, ServerPlayer player) {
        try {
            T converted = (T)data;
            updateWith(converted, player);
        } catch (ClassCastException ignored) {}
    }

    public void complete(ServerPlayer player, boolean announce) {
        CompleteTaskPayload payload;
        if(player == null) {
            setCompleted(false, null);
            payload = new CompleteTaskPayload(getId(), -1, null, false);
        } else {
            Optional<? extends LockoutTeam> team = LockoutServer.lockout.getTeams().stream()
                    .filter(t -> t.getPlayerIds().stream().anyMatch(i -> i.equals(player.getUUID())))
                    .findAny();
            if(team.isEmpty()) return;
            setCompleted(true, team.get());
            int teamIndex = LockoutServer.lockout.getTeams().indexOf(team.get());
            payload = new CompleteTaskPayload(getId(), teamIndex, player.getName().getString(), announce);
            LockoutServer.lockout.evaluateWinnerAndEndGame(team.get());
        }

        for(ServerPlayer serverPlayer : LockoutServer.server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }

    }

    public JsonElement serialize(List<? extends LockoutTeam> teams, boolean includeState) {
        JsonObject object = new JsonObject();
        object.add("id", new JsonPrimitive(getBuildData().getA()));
        if(getBuildData().getB() != null) {
            object.add("option", new JsonPrimitive(getBuildData().getB()));
        }
        if(includeState) {
            object.add("completed", new JsonPrimitive(isCompleted()));
            if (getCompletedTeam() != null) {
                int completedTeamIndex = teams.indexOf(getCompletedTeam());
                object.add("completedTeam", new JsonPrimitive(completedTeamIndex));
            }
            String progress = getProgress();
            if (progress != null) {
                object.add("progress", new JsonPrimitive(progress));
            }
        }
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal<?> goal = (Goal<?>) o;
        return id.equals(goal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Goal<?> deserialize(JsonElement data, List<? extends LockoutTeam> teams) throws IOException {
        if(!data.isJsonObject()) throw new IOException("Goal data is not valid.");
        JsonObject goalData = data.getAsJsonObject();

        JsonElement id = goalData.get("id");
        if(id == null || !id.isJsonPrimitive() || !id.getAsJsonPrimitive().isString()) throw new IOException("Goal data does not contain valid id.");
        GoalBuilder<?> builder = GoalRegistry.INSTANCE.get(id.getAsString());
        if(builder == null) throw new IOException("Goal referenced by id could not be found.");

        String option = null;
        JsonElement optionData = goalData.get("option");
        if(optionData != null)  {
            if(!optionData.isJsonPrimitive() || !optionData.getAsJsonPrimitive().isString()) throw new IOException("Goal data option is invalid.");
            option = optionData.getAsString();
        }
        Goal<?> goal;
        try {
            goal = builder.buildFromSerializedData(option);
        } catch (IllegalGoalConstructionException e) {
            throw new IOException("Failed to construct goal.", e);
        }

        JsonElement completed = goalData.get("completed");
        if(completed != null) {
            if (!completed.isJsonPrimitive() || !completed.getAsJsonPrimitive().isBoolean())
                throw new IOException("Goal data does not contain valid completed state.");
            LockoutTeam team = null;
            JsonElement teamData = goalData.get("completedTeam");
            if (teamData != null) {
                if (!teamData.isJsonPrimitive() || !teamData.getAsJsonPrimitive().isNumber())
                    throw new IOException("Goal data does completedTeam is invalid.");
                team = teams.get(teamData.getAsInt());
            }
            goal.setCompleted(completed.getAsBoolean(), team);
        }

        JsonElement progress = goalData.get("progress");
        if(progress != null) {
            if(!progress.isJsonPrimitive() || !progress.getAsJsonPrimitive().isString()) throw new IOException("Goal data progress is invalid.");
            goal.setProgress(progress.getAsString());
        }

        return goal;
    }
}

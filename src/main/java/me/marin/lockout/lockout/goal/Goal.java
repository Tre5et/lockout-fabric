package me.marin.lockout.lockout.goal;

import lombok.Getter;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
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

    public void setProgress(String progress) {}

    public void sendProgress(ServerPlayer player) {}

    public abstract void updateWith(T data, ServerPlayer player);

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
}

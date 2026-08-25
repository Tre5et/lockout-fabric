package me.marin.lockout.lockout.goal;

import lombok.Getter;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Objects;

public abstract class Goal {
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

    public Goal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, List<GoalHint> hints, Pair<String, String> buildData) {
        this.id = id;
        this.nameExtractor = nameExtractor;
        this.textureExtractor = textureExtractor;
        this.hints = hints;
        this.buildData = buildData;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return id.equals(goal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

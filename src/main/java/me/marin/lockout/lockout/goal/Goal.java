package me.marin.lockout.lockout.goal;

import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Objects;

public abstract class Goal {
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final TooltipInfo tooltipInfo;
    @Getter
    private boolean isCompleted = false;
    @Getter
    private LockoutTeam completedTeam;
    private final TextureRenderer textureRenderer;
    @Getter
    private final List<GoalHint> hints;

    @Getter
    // TODO: improve build data serialization
    private final Pair<String, String> buildData;

    public Goal(String id, String name, TooltipInfo tooltipInfo, TextureRenderer textureRenderer, List<GoalHint> hints, Pair<String, String> buildData) {
        this.id = id;
        this.name = name;
        this.tooltipInfo = tooltipInfo;
        this.textureRenderer = textureRenderer;
        this.hints = hints;
        this.buildData = buildData;
    }

    public void setCompleted(boolean isCompleted, LockoutTeam team) {
        this.isCompleted = isCompleted;
        this.completedTeam = team;
    }

    public final void render(GuiGraphicsExtractor extractor, Font font, int x, int y) {
        textureRenderer.renderTexture(extractor, font, x, y, LockoutClient.CURRENT_TICK);
    }

    public final List<String> getTooltip(LockoutTeam team, Player player) {
        return getTooltipInfo().get(this, team, player);
    }

    public final List<String> getSpectatorTooltip() {
        return getTooltipInfo().getSpectator(this);
    }

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

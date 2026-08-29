package me.marin.lockout.client.goal.builder;

import lombok.Getter;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientGoalBuildParameters extends GoalBuildParameters {
    @Getter
    private final Component name;
    @Getter
    private final TextureExtractor textureExtractor;
    @Getter
    private final ClientGoalProgress<?> progress;
    @Getter
    private final List<ClientHint<?>> hints;

    public ClientGoalBuildParameters(String id, BuildData buildData, Component name, TextureExtractor textureExtractor, ClientGoalProgress<?> progress, List<ClientHint<?>> hints) {
        super(id, buildData);
        this.name = name;
        this.textureExtractor = textureExtractor;
        this.progress = progress;
        this.hints = hints;
    }
}

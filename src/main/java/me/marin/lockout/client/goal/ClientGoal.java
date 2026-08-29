package me.marin.lockout.client.goal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.goal.builder.ClientGoalBuildParameters;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.render.ColoredTriangleRenderState;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.BuildData;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.joml.Matrix3x2f;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientGoal extends Goal {
    @Getter
    private final Component name;
    private final TextureExtractor textureExtractor;
    @Getter
    private final ClientGoalProgress<?> progress;
    @Getter
    private final List<ClientHint<?>> hints;

    public ClientGoal(ClientGoalBuildParameters parameters) {
        super(parameters);
        this.name = parameters.getName();
        this.textureExtractor = parameters.getTextureExtractor();
        this.progress = parameters.getProgress();
        this.hints = parameters.getHints();
    }

    public void extractTexture(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        textureExtractor.extract(extractor, font, x, y, width, height, tick);
    }

    public void extractTooltip(GuiGraphicsExtractor extractor, Font font, int x, int y, boolean includeProgress, boolean includeHint, Optional<LockoutTeam> playerTeam, Component customEnding) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getName());

        if(includeProgress) {
            List<Component> progressTooltip = new ArrayList<>();
            if(playerTeam.isPresent()) {
                progressTooltip.addAll(getProgress().getTooltip(playerTeam.get(), LockoutClient.lockout));
            } else {
                progressTooltip.addAll(getProgress().getSpectatorTooltip(LockoutClient.lockout));
            }
            if(!progressTooltip.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.addAll(progressTooltip.stream().map(c -> c.copy().withColor(TextColor.GRAY)).toList());
            }
        }

        if(includeHint && playerTeam.isPresent()) {
            List<ClientHint<?>> hints = getHints();
            if(!hints.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Hints:").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.ITALIC).withColor(TextColor.GRAY));
                for(int i = 0; i < hints.size(); i++) {
                    tooltip.add(hints.get(i).extract(LockoutClient.getHintKeys().get(i)));
                }
            }
        }

        if(customEnding != null) {
            tooltip.add(customEnding);
        }

        extractor.setTooltipForNextFrame(font, tooltip, Optional.empty(), x, y);
    }

    public void extractBackground(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height) {
        List<LockoutTeam> completedTeams = getProgress().getCompletedTeams(LockoutClient.lockout);
        if(completedTeams.isEmpty()) return;

        if(completedTeams.size() == 1) {
            extractor.fill(x, y, x + width, y + height, 0xFF000000 | completedTeams.getFirst().getColor().rgb());
/*            extractor.guiRenderState.addGuiElement(new ColoredTriangleRenderState(
                    RenderPipelines.GUI, TextureSetup.noTexture(),
                    new Matrix3x2f(extractor.pose()),
                    x, y,
                    x, y + height,
                    x + width, y,
                    0xFF000000 | completedTeams.getFirst().getColor().rgb(),
                    extractor.scissorStack.peek()
            ));*/
        } else if(completedTeams.size() == 2) {
            extractor.guiRenderState.addGuiElement(new ColoredTriangleRenderState(
                    RenderPipelines.GUI, TextureSetup.noTexture(),
                    new Matrix3x2f(extractor.pose()),
                    x, y,
                    x, y + height,
                    x + width, y,
                    0xFF000000 | completedTeams.getFirst().getColor().rgb(),
                    extractor.scissorStack.peek()
            ));
            extractor.guiRenderState.addGuiElement(new ColoredTriangleRenderState(
                    RenderPipelines.GUI, TextureSetup.noTexture(),
                    new Matrix3x2f(extractor.pose()),
                    x + width, y,
                    x, y + height,
                    x + width, y + height,
                    0xFF000000 | completedTeams.get(1).getColor().rgb(),
                    extractor.scissorStack.peek()
            ));
        } else if(completedTeams.size() == 3) {
            extractor.fill(x, y, x + width, y + height, 0xFF000000 | completedTeams.get(1).getColor().rgb());
            extractor.guiRenderState.addGuiElement(new ColoredTriangleRenderState(
                    RenderPipelines.GUI, TextureSetup.noTexture(),
                    new Matrix3x2f(extractor.pose()),
                    x, y,
                    x, y + (int)(height * 0.666),
                    x + (int)(width * 0.666), y,
                    0xFF000000 | completedTeams.getFirst().getColor().rgb(),
                    extractor.scissorStack.peek()
            ));
            extractor.guiRenderState.addGuiElement(new ColoredTriangleRenderState(
                    RenderPipelines.GUI, TextureSetup.noTexture(),
                    new Matrix3x2f(extractor.pose()),
                    x + width, y + (int)(height * 0.333),
                    x + (int)(width * 0.333), y + height,
                    x + width, y + height,
                    0xFF000000 | completedTeams.get(2).getColor().rgb(),
                    extractor.scissorStack.peek()
            ));
        } else if(completedTeams.size() == 4) {
            extractor.fill(x, y, x + (int)(width * 0.5), y + (int)(height * 0.5), 0xFF000000 | completedTeams.getFirst().getColor().rgb());
            extractor.fill(x + (int)(width * 0.5), y, x + width, y + (int)(height * 0.5), 0xFF000000 | completedTeams.get(1).getColor().rgb());
            extractor.fill(x, y + (int)(height * 0.5), x + (int)(width * 0.5), y + height, 0xFF000000 | completedTeams.get(2).getColor().rgb());
            extractor.fill(x + (int)(width * 0.5), y + (int)(height * 0.5), x + width, y + height, 0xFF000000 | completedTeams.get(3).getColor().rgb());
        } else {
            for(int i = 0; i < completedTeams.size(); i++) {
                extractor.fill(x + (int)((float)width / completedTeams.size() * i), y, x + (int)((float)width / completedTeams.size() * (i+1)), y + height, 0xFF000000 | completedTeams.get(i).getColor().rgb());
            }
        }
    }

    public void updateProgress(String progress) throws IllegalArgumentException {
        JsonElement progressJson = JsonParser.parseString(progress);
        this.progress.deserialize(progressJson);
    }

    public static ClientGoal deserialize(JsonElement element) throws IllegalGoalConstructionException {
        if (element == null || !element.isJsonObject())
            throw new IllegalGoalConstructionException("Server goal data is not an object.");
        JsonObject data = element.getAsJsonObject();

        if (!data.has("id") || !data.get("id").isJsonPrimitive() || !data.get("id").getAsJsonPrimitive().isString()) throw new IllegalGoalConstructionException("Server goal data does not contain a valid id.");
        if (!GoalRegistry.INSTANCE.isRegistered(data.get("id").getAsString())) throw new IllegalGoalConstructionException("Goal with id " + data.get("id").getAsString() + " is not registered.");
        GoalBuilder<?,?> builder = GoalRegistry.INSTANCE.get(data.get("id").getAsString());

        String option = null;
        if(data.has("option")) {
            JsonElement optionElement = data.get("option");
            if(!optionElement.isJsonPrimitive() || !optionElement.getAsJsonPrimitive().isString()) throw new IllegalGoalConstructionException("Server goal data option is invalid.");
            option = optionElement.getAsString();
        }
        ClientGoal goal = builder.buildClientFromSerializedData(option);
        if(data.has("progress")) {
            try {
                goal.getProgress().deserialize(data.get("progress"));
            } catch (IllegalArgumentException e) {
                throw new IllegalGoalConstructionException("Failed to deserialize goal progress", e);
            }
        }
        return goal;
    }

    public static List<ClientGoal> constructAll(List<BuildData> data) throws IllegalGoalConstructionException {
        List<ClientGoal> goals = new ArrayList<>();
        List<Pair<BuildData, Exception>> invalidGoals = new ArrayList<>();
        for(BuildData goal : data) {
            if(!GoalRegistry.INSTANCE.isRegistered(goal.id())) {
                invalidGoals.add(new Pair<>(goal, new IllegalGoalConstructionException("Goal does not exist")));
            } else {
                try {
                    goals.add(GoalRegistry.INSTANCE.get(goal.id()).buildClientFromSerializedData(goal.option().orElse(null)));
                } catch (IllegalGoalConstructionException e) {
                    invalidGoals.add(new Pair<>(goal, e));
                }
            }
        }
        if(!invalidGoals.isEmpty()) {
            throw new IllegalGoalConstructionException("Failed to construct some goals: " +
                    invalidGoals.stream()
                            .map(g -> g.getA().id() + " (" + g.getA().option().orElse("null") + "): " + g.getB().getMessage())
                            .collect(Collectors.joining("; "))
            );
        }
        return goals;
    }
}

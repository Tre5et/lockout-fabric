package me.marin.lockout;

import me.marin.lockout.client.LockoutBoard;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.gui.BoardBuilderScreen;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.marin.lockout.Constants.*;
import static me.marin.lockout.LockoutConfig.BoardPosition.LEFT;

public class Utility {

    public static int NEUTRAL = 0xFF000000;

    public static void drawBingoBoard(GuiGraphicsExtractor context) {
        LockoutConfig.BoardPosition boardPosition = LockoutConfig.getInstance().boardPosition;

        // Don't render board if F3 is open with left-side board.
        if (boardPosition == LockoutConfig.BoardPosition.LEFT && Minecraft.getInstance().gui.hud.getDebugOverlay().showDebugScreen()) {
            return;
        }

        Font textRenderer = Minecraft.getInstance().font;

        Lockout lockout = LockoutClient.lockout;
        LockoutBoard board = lockout.getBoard();

        int boardWidth = 2 * GUI_PADDING + board.size() * GUI_SLOT_SIZE;
        int boardHeight = GUI_PADDING + GUI_PADDING_BOTTOM + board.size() * GUI_SLOT_SIZE;

        int boardRightEdgeX = boardPosition == LEFT ? boardWidth : context.guiWidth();
        int boardLeftEdgeX = boardRightEdgeX - boardWidth;

        int x = boardLeftEdgeX;
        int y = 0;

        context.blitSprite(RenderPipelines.GUI_TEXTURED, Constants.GUI_IDENTIFIER, x, y, boardWidth, boardHeight);

        x += GUI_PADDING + 1;
        y += GUI_PADDING + 1;
        final int startX = x;

        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                Goal goal = board.getGoals().get(j + board.size() * i);
                if (goal != null) {
                    if (goal.isCompleted()) {
                        context.fill(x, y, x + 16, y + 16, NEUTRAL | goal.getCompletedTeam().getColor().textColor().getValue());
                    }

                    goal.render(context, textRenderer, x, y);

                }
                x += GUI_SLOT_SIZE;
            }
            y += GUI_SLOT_SIZE;
            x = startX;
        }
        x += 2;
        y += 1;
        MutableComponent formattedPoints = Component.empty();
        for (LockoutTeam team : lockout.getTeams()) {
            if(!formattedPoints.equals(Component.empty())) {
                formattedPoints.append(
                        Component.literal("-").withColor(TextColor.WHITE)
                );
            }
            formattedPoints.append(
                    Component.literal(String.valueOf(team.getPoints())).withColor(team.getColor().textColor())
            );
        }

        if(formattedPoints != null) context.text(textRenderer, formattedPoints, x, y, NEUTRAL, true);

        Component timer = Component.literal(Utility.ticksToTimer(lockout.getTicks())).withColor(TextColor.WHITE);
        context.text(textRenderer, timer, boardRightEdgeX - textRenderer.width(timer) - 4, y, NEUTRAL, true);

        List<Component> formattedNames = new ArrayList<>();
        int maxWidth = 0;
        for (LockoutTeam team : lockout.getTeams()) {
            for (String playerName : team.getPlayerNames()) {
                formattedNames.add(Component.literal(playerName).withColor(team.getColor().textColor()));
                maxWidth = Math.max(maxWidth, textRenderer.width(playerName));
            }
        }

        y += 20;
        switch (boardPosition) {
            case RIGHT -> {
                context.fill(context.guiWidth() - maxWidth - 3 - 1,  y - 2, context.guiWidth() - 1, y + formattedNames.size() * textRenderer.lineHeight + 1, 0x80_00_00_00);

                for (Component formattedName : formattedNames) {
                    context.text(textRenderer, formattedName, context.guiWidth() - textRenderer.width(formattedName) - 2, y, NEUTRAL, true);
                    y += textRenderer.lineHeight;
                }
            }
            case LEFT -> {
                context.fill(1,  y - 2, 4 + maxWidth, y + formattedNames.size() * textRenderer.lineHeight + 1, 0x80_00_00_00);

                for (Component formattedName : formattedNames) {
                    context.text(textRenderer, formattedName, 3, y, NEUTRAL, true);
                    y += textRenderer.lineHeight;
                }
            }
        }

    }

    public static void drawCenterBingoBoard(GuiGraphicsExtractor context, Font textRenderer, int mouseX, int mouseY) {
        int width = context.guiWidth();
        int height = context.guiHeight();

        LockoutBoard board = LockoutClient.lockout.getBoard();

        int boardWidth = 2 * GUI_CENTER_PADDING + board.size() * GUI_CENTER_SLOT_SIZE;
        int x = width / 2 - boardWidth / 2;

        int boardHeight = 2 * GUI_CENTER_PADDING + board.size() * GUI_CENTER_SLOT_SIZE;
        int y = height / 2 - boardHeight / 2;

        context.blitSprite(RenderPipelines.GUI_TEXTURED, GUI_CENTER_IDENTIFIER, x, y, boardWidth, boardHeight);

        x += GUI_CENTER_PADDING + 1;
        y += GUI_CENTER_PADDING + 1;
        final int startX = x;

        Goal hoveredGoal = getBoardHoveredGoal(context, mouseX, mouseY);

        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                Goal goal = board.getGoals().get(j + board.size() * i);
                if (goal != null) {
                    if (goal.isCompleted()) {
                        context.fill(x, y, x + 16, y + 16, (0xFF << 24) | goal.getCompletedTeam().getColor().textColor().getValue());
                    }

                    goal.render(context, textRenderer, x, y);

                    if (goal == hoveredGoal) {
                        context.fill(x, y, x + 16, y + 16, GUI_CENTER_HOVERED_COLOR);
                    }
                }
                x += GUI_CENTER_SLOT_SIZE;
            }
            y += GUI_CENTER_SLOT_SIZE;
            x = startX;
        }
    }

    public static Optional<Integer> getBoardHoveredIndex(int size, int width, int height, int mouseX, int mouseY) {
        int x = width / 2 - (2 * GUI_CENTER_PADDING + size * GUI_CENTER_SLOT_SIZE) / 2 + GUI_CENTER_PADDING - BoardBuilderScreen.CENTER_OFFSET;
        int y = height / 2 - (2 * GUI_CENTER_PADDING + size * GUI_CENTER_SLOT_SIZE) / 2 + GUI_CENTER_PADDING;
        final int startX = x;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (mouseX >= x-1 && mouseX < x + GUI_CENTER_SLOT_SIZE && mouseY >= y-1 && mouseY < y + GUI_CENTER_SLOT_SIZE) {
                    return Optional.of(j + i * size);
                }
                x += GUI_CENTER_SLOT_SIZE;
            }
            y += GUI_CENTER_SLOT_SIZE;
            x = startX;
        }

        return Optional.empty();
    }

    public static Goal getBoardHoveredGoal(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Optional<Integer> hoveredIdx = getBoardHoveredIndex(LockoutClient.lockout.getBoard().size(), context.guiWidth(), context.guiHeight(), mouseX, mouseY);
        return hoveredIdx.map(integer -> LockoutClient.lockout.getBoard().getGoals().get(integer)).orElse(null);
    }

    public static void drawGoalInformation(GuiGraphicsExtractor context, Font textRenderer, Goal goal, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();
        tooltip.add(Component.nullToEmpty((goal.getTooltipInfo() != null ? ChatFormatting.UNDERLINE : "") + goal.getName()).getVisualOrderText());
        if (goal.getTooltipInfo() != null && LockoutClient.goalTooltipMap.containsKey(goal.getId())) {
            for (String t : LockoutClient.goalTooltipMap.get(goal.getId()).split("\n")) {
                tooltip.add(Component.nullToEmpty(t).getVisualOrderText());
            }
        }

        // Add hint prompts / results
        List<GoalHint> hints = goal.getHints();
        if (!hints.isEmpty() && LockoutClient.amIPlayingLockout) {
            tooltip.add(Component.empty().getVisualOrderText());
            tooltip.add(Component.nullToEmpty("" + ChatFormatting.GRAY + ChatFormatting.UNDERLINE + ChatFormatting.ITALIC + "Hints:").getVisualOrderText());

            Map<Integer, String> hintResults = LockoutClient.goalHintResults.get(goal.getId());
            Map<Integer, String> hintErrors = LockoutClient.goalHintErrors.get(goal.getId());
            for (int i = 0; i < hints.size(); i++) {
                if (hintResults != null && hintResults.containsKey(i)) {
                    tooltip.add(Component.nullToEmpty(ChatFormatting.AQUA + hintResults.get(i)).getVisualOrderText());
                } else if (hintErrors != null && hintErrors.containsKey(i)) {
                    tooltip.add(Component.nullToEmpty(ChatFormatting.RED + hints.get(i).getName() + ": " + hintErrors.get(i)).getVisualOrderText());
                } else {
                    tooltip.add(Component.nullToEmpty(ChatFormatting.GRAY + hints.get(i).getName() + ": Press " + (i + 1)).getVisualOrderText());
                }
            }
        }

        context.setTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
    }

    /**
     * Code from {@link GuiGraphicsExtractor#itemCount(Font, ItemStack, int, int, String)} (Font, ItemStack, int, int, String)}, but without ItemStack argument requirement
     */
    public static void drawStackCount(GuiGraphicsExtractor context, int x, int y, String count) {
        Font textRenderer = Minecraft.getInstance().font;
        context.pose().pushMatrix();
        context.pose().translate(0.0F, 0.0F);
        context.text(textRenderer, count, x + 19 - 2 - textRenderer.width(count), y + 6 + 3, -1, true);
        context.pose().popMatrix();
    }

    public static List<ServerPlayer> getSpectators(Lockout lockout, MinecraftServer server) {
        return server.getPlayerList().getPlayers()
                .stream()
                .filter(p -> !lockout.isLockoutPlayer(p.getUUID()))
                .toList();
    }

    public static String ticksToTimer(long ticks) {
        ticks = Math.abs(ticks);
        long second = (ticks / 20) % 60;
        long minute = ((ticks / 20) / 60) % 60;
        long hour = ((ticks / 20) / 60 / 60) % 24;

        String time;
        if (hour > 0) {
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            time = String.format("%02d:%02d", minute, second);
        }

        return time;
    }

}

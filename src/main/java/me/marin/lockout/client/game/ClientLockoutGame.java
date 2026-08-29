package me.marin.lockout.client.game;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutConfig;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutGame;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientLockoutGame extends LockoutGame<ClientLockoutBoard> {
    private static final int GOAL_SIZE = 16;
    private static final int GOAL_PADDING = 2;
    private static final int BOARD_PADDING = 3;
    private static final int BOARD_PADDING_BOTTOM = 13;

    public ClientLockoutGame(ClientLockoutBoard board, List<? extends LockoutTeam> teams) {
        super(board, teams);
    }

    public void extractHud(GuiGraphicsExtractor extractor, Font font, LockoutConfig.BoardPosition position) {
        extractBoard(extractor, font, position);
        extractPlayerList(extractor, font, position);
    }

    private void extractBoard(GuiGraphicsExtractor extractor, Font font, LockoutConfig.BoardPosition position) {
        if (position == LockoutConfig.BoardPosition.LEFT && Minecraft.getInstance().gui.hud.getDebugOverlay().showDebugScreen()) {
            return;
        }

        int boardSize = getBoard().getSize() * (GOAL_SIZE + GOAL_PADDING) - GOAL_PADDING;
        int boardX = switch(position) {
            case LEFT ->  BOARD_PADDING;
            case RIGHT -> extractor.guiWidth() - boardSize - BOARD_PADDING;
        };

        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, Constants.GUI_IDENTIFIER, boardX - BOARD_PADDING, 0, boardSize + 2 * BOARD_PADDING, boardSize + BOARD_PADDING + BOARD_PADDING_BOTTOM);

        getBoard().extractGoals(extractor, font, boardX, BOARD_PADDING, GOAL_SIZE, GOAL_PADDING, (x,y,i,g) -> g.ifPresent(goal ->
            goal.extractBackground(extractor, font, x, y, GOAL_SIZE, GOAL_SIZE)
        ), (_,_,_,_) -> {});

        extractor.text(font, getPointsDisplay(), boardX, boardSize + BOARD_PADDING + 2, 0xFF000000);

        Component time = getTimer();
        extractor.text(font, getTimer(), boardX + boardSize - font.width(time), boardSize + BOARD_PADDING + 2, 0xFF000000);

    }

    private void extractPlayerList(GuiGraphicsExtractor extractor, Font font, LockoutConfig.BoardPosition position) {
        Pair<List<Component>, Integer> data = getPlayerNames(font);
        List<Component> names = data.getA();
        int maxWidth = data.getB();

        int y = extractor.guiHeight() / 2 - (names.size() * font.lineHeight) / 2;

        switch(position) {
            case LEFT -> {
                extractor.fill(1, y - 2, maxWidth + 4, y + names.size() * font.lineHeight + 1, 0x80000000);
                for(Component name : names) {
                    extractor.text(font, name, 1, y, 0xFF000000);
                    y += font.lineHeight;
                }
            }
            case RIGHT -> {
                extractor.fill(extractor.guiWidth() - maxWidth - 4, y - 2, extractor.guiWidth() - 1, y + names.size() * font.lineHeight + 1, 0x80000000);
                for(Component name : names) {
                    extractor.text(font, name, extractor.guiWidth() - font.width(name) - 1, y, 0xFF000000);
                    y += font.lineHeight;
                }
            }
        }
    }

    public Component getTimer() {
        long ticks = Math.abs(getTicks());
        long second = (ticks / 20) % 60;
        long minute = ((ticks / 20) / 60) % 60;
        long hour = ((ticks / 20) / 60 / 60) % 24;

        String time;
        if (hour > 0) {
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            time = String.format("%02d:%02d", minute, second);
        }

        MutableComponent component = Component.literal(time).withColor(TextColor.WHITE);
        if(getTicks() < 0) component.withStyle(ChatFormatting.ITALIC);
        return component;
    }

    public Component getPointsDisplay() {
        List<Map.Entry<LockoutTeam, Integer>> teams = getPoints().entrySet().stream().toList();

        MutableComponent component = Component.empty();
        for(int i = 0; i < teams.size(); i++) {
            if(i != 0) component.append(Component.literal("-").withColor(TextColor.WHITE));
            component.append(Component.literal(teams.get(i).getValue().toString()).withColor(teams.get(i).getKey().getColor().textColor()));
        }
        return component;
    }

    public Pair<List<Component>, Integer> getPlayerNames(Font font) {
        List<Component> formattedNames = new ArrayList<>();
        int maxWidth = 0;
        for (LockoutTeam team : getTeams()) {
            for (String playerName : team.getPlayerNames()) {
                formattedNames.add(Component.literal(playerName).withColor(team.getColor().textColor()));
                maxWidth = Math.max(maxWidth, font.width(playerName));
            }
        }
        return new Pair<>(formattedNames, maxWidth);
    }
}

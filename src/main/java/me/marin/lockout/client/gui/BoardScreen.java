package me.marin.lockout.client.gui;

import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.game.ClientLockoutBoard;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.game.GameState;
import me.marin.lockout.network.RequestHintPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static me.marin.lockout.Constants.GUI_CENTER_HOVERED_COLOR;
import static me.marin.lockout.Constants.GUI_CENTER_IDENTIFIER;

public class BoardScreen extends AbstractContainerScreen<BoardScreenHandler> {
    private static final int GOAL_SIZE = 16;
    private static final int GOAL_PADDING = 2;
    private static final int BOARD_PADDING = 8;

    private Optional<ClientGoal> hoveredGoal;

    public BoardScreen(BoardScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        if (LockoutClient.lockout == null) {
            this.onClose();
            return;
        }
        this.extractBackground(extractor, mouseX, mouseY, delta);
        Font textRenderer = Minecraft.getInstance().font;
        ClientLockoutBoard board = LockoutClient.lockout.getBoard();

        int centerOffset = (board.getSize() * (GOAL_SIZE + GOAL_PADDING) - GOAL_PADDING) / 2;
        int boardX = extractor.guiWidth() / 2 - centerOffset;
        int boardY = extractor.guiHeight() / 2 - centerOffset;

        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, GUI_CENTER_IDENTIFIER, boardX - BOARD_PADDING, boardY - BOARD_PADDING, 2 * BOARD_PADDING + board.getSize() * (GOAL_SIZE + GOAL_PADDING) - GOAL_PADDING, 2 * BOARD_PADDING + board.getSize() * (GOAL_SIZE + GOAL_PADDING) - GOAL_PADDING);
        hoveredGoal = board.getHoveredGoal(mouseX, mouseY, boardX, boardY, GOAL_SIZE, GOAL_PADDING);
        board.extractGoals(extractor, textRenderer, boardX, boardY, GOAL_SIZE, GOAL_PADDING,
                (x,y,_,g) -> g.ifPresent(goal -> goal.extractBackground(extractor, font, x, y, GOAL_SIZE, GOAL_SIZE)),
                (x,y,_,goal) -> {
                    if(goal.isPresent() && hoveredGoal.isPresent() && goal.get().equals(hoveredGoal.get())) {
                        extractor.fill(x, y, x + GOAL_SIZE, y + GOAL_SIZE, GUI_CENTER_HOVERED_COLOR);
                    }
                }
        );
        hoveredGoal.ifPresent(clientGoal -> clientGoal.extractTooltip(extractor, textRenderer, mouseX, mouseY, LockoutClient.lockout.getState() != GameState.STARTING, LockoutClient.lockout.getState() != GameState.STARTING, Optional.ofNullable(LockoutClient.playerTeam), null));
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        // Check if the pressed key matches the board keybinding
        if (LockoutClient.getBoardKeybinding().matches(input)) {
            this.onClose();
            return true;
        }

        // Handle hint keys 1-9 when hovering a goal
        Integer hintIndex = null;
        for(int i = 0; i < LockoutClient.getHintKeys().size(); i++) {
            if(LockoutClient.getHintKeys().get(i).matches(input)) {
                hintIndex = i;
                break;
            }
        }
        if (hintIndex != null && hoveredGoal.isPresent()) {
            if (hintIndex < hoveredGoal.get().getHints().size()) {
                ClientPlayNetworking.send(new RequestHintPayload(hoveredGoal.get().getId(), hintIndex));
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        LockoutClient.lockout.getBoard().getGoals().forEach(g -> g.getHints().forEach(h -> h.setError(null)));
        super.onClose();
    }

}

package me.marin.lockout.client.gui;

import me.marin.lockout.Lockout;
import me.marin.lockout.Utility;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.network.RequestHintPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class BoardScreen extends AbstractContainerScreen<BoardScreenHandler> {

    private Goal hoveredGoal;

    public BoardScreen(BoardScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!Lockout.exists(LockoutClient.lockout)) {
            this.onClose();
            return;
        }
        this.extractBackground(context, mouseX, mouseY, delta);
        Font textRenderer = Minecraft.getInstance().font;

        Utility.drawCenterBingoBoard(context, textRenderer, mouseX, mouseY);
        hoveredGoal = Utility.getBoardHoveredGoal(context, mouseX, mouseY);
        if (hoveredGoal != null) {
            Utility.drawGoalInformation(context, textRenderer, hoveredGoal, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        // Check if the pressed key matches the board keybinding
        if (LockoutClient.getBoardKeybinding().matches(input)) {
            this.onClose();
            return true;
        }

        // Handle hint keys 1-9 when hovering a goal
        int key = input.key();
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9 && hoveredGoal != null) {
            int hintIndex = key - GLFW.GLFW_KEY_1;
            if (hintIndex < hoveredGoal.getHints().size()) {
                ClientPlayNetworking.send(new RequestHintPayload(hoveredGoal.getId(), hintIndex));
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        LockoutClient.goalHintErrors.clear();
        super.onClose();
    }

}

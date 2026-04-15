package me.marin.lockout.client.gui;

import me.marin.lockout.Lockout;
import me.marin.lockout.Utility;
import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.lockout.Goal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BoardScreen extends AbstractContainerScreen<BoardScreenHandler> {

    public BoardScreen(BoardScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!Lockout.exists(LockoutClient.lockout)) {
            this.onClose();
            return;
        }
        this.renderBackground(context, mouseX, mouseY, delta);
        Font textRenderer = Minecraft.getInstance().font;

        Utility.drawCenterBingoBoard(context, textRenderer, mouseX, mouseY);
        Goal hoveredGoal = Utility.getBoardHoveredGoal(context, mouseX, mouseY);
        if (hoveredGoal != null) {
            Utility.drawGoalInformation(context, textRenderer, hoveredGoal, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {

    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        // Check if the pressed key matches the board keybinding
        if (LockoutClient.getBoardKeybinding().matches(input)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(input);
    }

}

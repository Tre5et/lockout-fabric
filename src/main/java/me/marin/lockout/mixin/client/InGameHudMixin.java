package me.marin.lockout.mixin.client;

import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutConfig;
import me.marin.lockout.Utility;
import me.marin.lockout.client.LockoutClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.marin.lockout.Constants.GUI_PADDING;
import static me.marin.lockout.Constants.GUI_SLOT_SIZE;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

    // Render the board after effects, but before chat, player list etc.
    @Inject(method = "extractRenderState", at = @At(value="INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    public void renderBoard(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Lockout.exists(LockoutClient.lockout)) {
            return;
        }

        Utility.drawBingoBoard(context);
    }

    // If lockout board is visible, render effects to the left of it.
    @Redirect(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiWidth()I"))
    private int renderStatusEffects(GuiGraphicsExtractor instance) {
        int width = instance.guiWidth();

        if (!Lockout.exists(LockoutClient.lockout)) {
            return width;
        }

        if (LockoutConfig.getInstance().boardPosition != LockoutConfig.BoardPosition.RIGHT) {
            return width;
        }

        return width - 2 * GUI_PADDING - LockoutClient.lockout.getBoard().size() * GUI_SLOT_SIZE;
    }

}

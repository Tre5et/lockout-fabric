package me.marin.lockout.mixin.client;

import me.marin.lockout.LockoutConfig;
import me.marin.lockout.client.LockoutClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.marin.lockout.Constants.GUI_PADDING;
import static me.marin.lockout.Constants.GUI_SLOT_SIZE;

@Mixin(Hud.class)
public abstract class HudMixin {

    // Render the board after effects, but before chat, player list etc.
    @Inject(method = "extractRenderState", at = @At(value="INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    public void renderBoard(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (LockoutClient.lockout == null) {
            return;
        }

        LockoutClient.lockout.extractHud(context, Minecraft.getInstance().font, LockoutConfig.getInstance().boardPosition);
    }

    // If lockout board is visible, render effects to the left of it.
    @Redirect(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiWidth()I"))
    private int renderStatusEffects(GuiGraphicsExtractor instance) {
        int width = instance.guiWidth();

        if (LockoutClient.lockout == null) {
            return width;
        }

        if (LockoutConfig.getInstance().boardPosition != LockoutConfig.BoardPosition.RIGHT) {
            return width;
        }

        return width - 2 * GUI_PADDING - LockoutClient.lockout.getBoard().getSize() * GUI_SLOT_SIZE;
    }

}

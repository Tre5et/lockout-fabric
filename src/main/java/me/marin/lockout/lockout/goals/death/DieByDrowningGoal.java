package me.marin.lockout.lockout.goals.death;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class DieByDrowningGoal extends Goal implements CustomTextureRenderer {

    private static final Identifier BUBBLE_TEXTURE = Identifier.withDefaultNamespace("mob_effect/water_breathing");
    private static final Identifier OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/overlay/die_to_overlay.png");

    public DieByDrowningGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Die by drowning";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    @Override
    public boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, BUBBLE_TEXTURE, x, y, 16, 16);
        context.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        return true;
    }
}

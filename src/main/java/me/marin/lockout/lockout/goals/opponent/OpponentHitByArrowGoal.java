package me.marin.lockout.lockout.goals.opponent;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.interfaces.OpponentGoal;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class OpponentHitByArrowGoal extends Goal implements OpponentGoal, CustomTextureRenderer {

    private static final ItemStack ITEM_STACK = Items.ARROW.getDefaultInstance();
    private static final Identifier ITEM_TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/arrow.png");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/overlay/no_overlay.png");

    public OpponentHitByArrowGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "All other opponents hit by Arrow";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

    @Override
    public boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        context.blit(RenderPipelines.GUI_TEXTURED, ITEM_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        return true;
    }

}

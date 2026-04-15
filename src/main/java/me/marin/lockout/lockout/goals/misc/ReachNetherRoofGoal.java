package me.marin.lockout.lockout.goals.misc;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.texture.CustomTextureRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ReachNetherRoofGoal extends Goal implements CustomTextureRenderer {

    private static final ItemStack ITEM_STACK = Items.BEDROCK.getDefaultInstance();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/overlay/up_overlay.png");

    public ReachNetherRoofGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Get on Nether Roof";
    }

    @Override
    public ItemStack getTextureItemStack() {
        return ITEM_STACK;
    }

    @Override
    public boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        context.renderItem(ITEM_STACK, x, y);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        return true;
    }

}
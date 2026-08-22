package me.marin.lockout.lockout.goals.kill;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.interfaces.KillMobGoal;
import me.marin.lockout.lockout.texture.TextureProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;

public class KillSlimeGoal extends KillMobGoal implements TextureProvider {

    public KillSlimeGoal(String id, String data) {
        super(id, data);
    }

    @Override
    public String getGoalName() {
        return "Kill Slime";
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/slime.png");
    @Override
    public Identifier getTextureIdentifier() {
        return TEXTURE;
    }

    @Override
    public EntityType<?> getEntity() {
        return EntityTypes.SLIME;
    }
    
    @Override
    public ItemStack getTextureItemStack() {
        return null;
    }

    private static final Identifier OVERLAY = Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/overlay/kill_overlay.png");
    
    @Override
    public boolean renderTexture(GuiGraphicsExtractor context, int x, int y, int tick) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0,0, 16, 16, 16, 16);
        context.blit(RenderPipelines.GUI_TEXTURED, OVERLAY, x, y, 0,0, 16, 16, 16, 16);
        return true;
    }
}


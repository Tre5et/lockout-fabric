package me.marin.lockout.lockout.texture;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public interface CycleTexturesProvider extends CustomTextureRenderer {

    List<Identifier> getTexturesToDisplay();

    @Override
    default boolean renderTexture(GuiGraphics context, int x, int y, int tick) {
        int mod = tick % (60 * getTexturesToDisplay().size());
        context.blit(RenderPipelines.GUI_TEXTURED, getTexturesToDisplay().get(mod / 60), x, y, 0, 0, 16, 16, 16, 16);
        return true;
    }

}

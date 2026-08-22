package me.marin.lockout.lockout.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GenericTextureRenderer implements TextureRenderer {
    private final Identifier texture;

    private GenericTextureRenderer(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void renderTexture(GuiGraphicsExtractor extractor, Font font, int x, int y, int tick) {
        extractor.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, 16, 16, 16, 16);
    }

    public static GenericTextureRenderer texture(Identifier texture) {
        return new GenericTextureRenderer(texture);
    }

    public static CycleTextureRenderer cycleTextures(Identifier... textures) {
        return CycleTextureRenderer.texture(textures);
    }
}

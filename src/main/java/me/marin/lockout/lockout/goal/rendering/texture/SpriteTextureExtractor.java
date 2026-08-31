package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

public class SpriteTextureExtractor implements TextureExtractor {
    private final Identifier sprite;

    public SpriteTextureExtractor(Identifier sprite) {
        this.sprite = sprite;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    public static SpriteTextureExtractor sprite(Identifier sprite) {
        return new SpriteTextureExtractor(sprite);
    }

    public static CycleTextureExtractor cycle(Identifier... sprites) {
        return new CycleTextureExtractor(Arrays.stream(sprites).map(SpriteTextureExtractor::sprite).toList());
    }
}

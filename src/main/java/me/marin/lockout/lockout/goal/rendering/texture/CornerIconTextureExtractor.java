package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CornerIconTextureExtractor implements TextureExtractor {
    private final TextureExtractor backgroundExtractor;
    private final TextureExtractor iconExtractor;
    private final int size;

    public CornerIconTextureExtractor(TextureExtractor backgroundExtractor, TextureExtractor iconExtractor, int size) {
        this.backgroundExtractor = backgroundExtractor;
        this.iconExtractor = iconExtractor;
        this.size = size;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        backgroundExtractor.extract(extractor, font, x, y, width, height, tick);
        iconExtractor.extract(extractor, font, x + width - size, y, size, size, tick);
    }
}

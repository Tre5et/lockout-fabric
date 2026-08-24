package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class StackingTextureExtractor implements TextureExtractor {
    private final List<TextureExtractor> extractors;
    private final int scaleFactor;

    public StackingTextureExtractor(List<TextureExtractor> extractors, int scaleFactor) {
        this.extractors = extractors;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, int tick) {
        int currentOffset = 0;
        for(TextureExtractor e : extractors) {
            e.extract(extractor, font, x + currentOffset, y + currentOffset, width - 2*currentOffset, height - 2*currentOffset, tick);
            currentOffset += scaleFactor;
        }
    }
}

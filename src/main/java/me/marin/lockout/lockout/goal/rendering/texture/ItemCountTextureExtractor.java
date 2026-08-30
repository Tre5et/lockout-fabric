package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ItemCountTextureExtractor implements TextureExtractor {
    private final Component text;

    public ItemCountTextureExtractor(Component text) {
        this.text = text;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.text(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, -1, true);
        });
    }
}

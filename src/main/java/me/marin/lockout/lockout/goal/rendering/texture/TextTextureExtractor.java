package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class TextTextureExtractor implements TextureExtractor {
    private final Component text;

    public TextTextureExtractor(Component text) {
        this.text = text;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, int tick) {
        float scale = Math.min((float)width / font.width(text.getVisualOrderText()), (float)height / font.lineHeight);

        withScale(extractor, x+width/2, y+height/2, scale, () -> extractor.centeredText(font, text, x+width/2, y+height/2-font.lineHeight/2, 0xFF000000));
    }

    public static TextTextureExtractor text(String text) {
        return new TextTextureExtractor(Component.literal(text).withColor(TextColor.WHITE));
    }
}

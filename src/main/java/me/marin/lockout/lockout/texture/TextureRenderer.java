package me.marin.lockout.lockout.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TextureRenderer {
    /**
     * Renders/displays any custom texture. This method is called every client tick, meaning that the texture can change.
     * @param tick Number of client ticks passed. This number increases every client tick.
     */
    void renderTexture(GuiGraphicsExtractor extractor, Font font, int x, int y, int tick);
}

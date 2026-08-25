package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TextureExtractor {
    /**
     * Renders/displays any custom texture. This method is called every client tick, meaning that the texture can change.
     * @param tick Number of client ticks passed. This number increases every client tick.
     */
    void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, int tick);

    default void withScale(GuiGraphicsExtractor extractor, int x, int y, float scale, Runnable function) {
        extractor.pose().pushMatrix();
        extractor.pose().scaleAround(scale, x, y);
        function.run();
        extractor.pose().popMatrix();
    }
}

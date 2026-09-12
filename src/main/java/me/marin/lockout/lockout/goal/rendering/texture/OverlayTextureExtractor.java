package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import oshi.util.tuples.Pair;

public class OverlayTextureExtractor implements TextureExtractor {
    private final TextureExtractor base;
    private final TextureExtractor overlay;
    private final TextureAnchor anchor;
    private final int size;

    public OverlayTextureExtractor(TextureExtractor base, TextureExtractor overlay, TextureAnchor anchor, int size) {
        this.base = base;
        this.overlay = overlay;
        this.anchor = anchor;
        this.size = size;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        base.extract(extractor,font,x,y,width,height,tick);
        Pair<Integer, Integer> coordinates = anchor.getAnchor(x,y,width,height,size,size);
        overlay.extract(extractor,font,coordinates.getA(),coordinates.getB(),size,size,tick);
    }
}

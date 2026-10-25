package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import oshi.util.tuples.Pair;

public class OverlayTextureExtractor implements TextureExtractor {
    public static final MapCodec<OverlayTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureExtractor.CODEC.fieldOf("background").forGetter(OverlayTextureExtractor::getBase),
            TextureExtractor.CODEC.fieldOf("overlay").forGetter(OverlayTextureExtractor::getOverlay),
            TextureAnchor.CODEC.fieldOf("anchor").forGetter(OverlayTextureExtractor::getAnchor),
            Codec.INT.fieldOf("width").forGetter(OverlayTextureExtractor::getTargetWidth),
            Codec.INT.fieldOf("height").forGetter(OverlayTextureExtractor::getTargetHeight)
    ).apply(instance, OverlayTextureExtractor::new));

    @Getter
    private final TextureExtractor base;
    @Getter
    private final TextureExtractor overlay;
    @Getter
    private final TextureAnchor anchor;
    @Getter
    private final int targetWidth;
    @Getter
    private final int targetHeight;

    public OverlayTextureExtractor(TextureExtractor base, TextureExtractor overlay, TextureAnchor anchor, int targetWidth, int targetHeight) {
        this.base = base;
        this.overlay = overlay;
        this.anchor = anchor;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        base.extract(extractor,font,x,y,width,height,tick);
        Pair<Integer, Integer> coordinates = anchor.getAnchor(x,y,width,height,targetWidth,targetHeight);
        overlay.extract(extractor,font,coordinates.getA(),coordinates.getB(),targetWidth,targetHeight,tick);
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CornerIconTextureExtractor implements TextureExtractor {
    public static final MapCodec<CornerIconTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureExtractor.CODEC.fieldOf("background").forGetter(CornerIconTextureExtractor::getBackgroundExtractor),
            TextureExtractor.CODEC.fieldOf("icon").forGetter(CornerIconTextureExtractor::getIconExtractor),
            Codec.INT.fieldOf("size").forGetter(CornerIconTextureExtractor::getSize)
    ).apply(instance, CornerIconTextureExtractor::new));

    @Getter
    private final TextureExtractor backgroundExtractor;
    @Getter
    private final TextureExtractor iconExtractor;
    @Getter
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

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

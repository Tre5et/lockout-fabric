package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class StackingTextureExtractor implements TextureExtractor {
    public static final MapCodec<StackingTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(TextureExtractor.CODEC).fieldOf("extractors").forGetter(StackingTextureExtractor::getExtractors),
            Codec.INT.fieldOf("scale_factor").forGetter(StackingTextureExtractor::getScaleFactor)
    ).apply(instance, StackingTextureExtractor::new));

    @Getter
    private final List<TextureExtractor> extractors;
    @Getter
    private final int scaleFactor;

    public StackingTextureExtractor(List<TextureExtractor> extractors, int scaleFactor) {
        this.extractors = extractors;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        int currentOffset = 0;
        for(TextureExtractor e : extractors) {
            e.extract(extractor, font, x + currentOffset, y + currentOffset, width - 2*currentOffset, height - 2*currentOffset, tick);
            currentOffset += scaleFactor;
        }
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

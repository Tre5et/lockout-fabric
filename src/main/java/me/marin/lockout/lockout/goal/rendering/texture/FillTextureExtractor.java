package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

public class FillTextureExtractor implements TextureExtractor {
    public static final MapCodec<FillTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(FillTextureExtractor::getColor)
    ).apply(instance, FillTextureExtractor::new));

    @Getter
    private final int color;

    public FillTextureExtractor(int color) {
        this.color = color;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        extractor.fill(x, y, x+width, y+height, ARGB.white(100));
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

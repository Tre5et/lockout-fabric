package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SpriteTextureExtractor implements TextureExtractor {
    public static final MapCodec<SpriteTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("sprite").forGetter(SpriteTextureExtractor::getSprite)
    ).apply(instance, SpriteTextureExtractor::new));

    @Getter
    private final Identifier sprite;

    public SpriteTextureExtractor(Identifier sprite) {
        this.sprite = sprite;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }

    public static SpriteTextureExtractor sprite(Identifier sprite) {
        return new SpriteTextureExtractor(sprite);
    }

    public static CycleTextureExtractor cycle(Identifier... sprites) {
        return new CycleTextureExtractor(Arrays.stream(sprites).map(SpriteTextureExtractor::sprite).collect(Collectors.toUnmodifiableList()));
    }
}

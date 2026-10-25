package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GenericTextureExtractor implements TextureExtractor {
    public static final MapCodec<GenericTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(GenericTextureExtractor::getTexture)
    ).apply(instance, GenericTextureExtractor::new));

    @Getter
    private final Identifier texture;

    private GenericTextureExtractor(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        extractor.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }

    public static GenericTextureExtractor texture(Identifier texture) {
        return new GenericTextureExtractor(texture);
    }

    public static CycleTextureExtractor cycleTextures(Identifier... textures) {
        return CycleTextureExtractor.texture(textures);
    }
}

package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public class TextTextureExtractor implements TextureExtractor {
    public static final MapCodec<TextTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(t -> t.getText().getString()),
            TextColor.CODEC.optionalFieldOf("color", TextColor.WHITE).forGetter(t -> Optional.ofNullable(t.getText().getStyle().getColor()).orElse(TextColor.WHITE))
    ).apply(instance, TextTextureExtractor::text));

    @Getter
    private final Component text;

    public TextTextureExtractor(Component text) {
        this.text = text;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min((float)width / font.width(text.getVisualOrderText()), (float)height / font.lineHeight);

        withScale(extractor, x+width/2, y+height/2, scale, () -> extractor.centeredText(font, text, x+width/2, y+height/2-font.lineHeight/2, 0xFF000000));
    }

    @Override
    public MapCodec<TextTextureExtractor> codec() {
        return MAP_CODEC;
    }

    public static TextTextureExtractor text(String text) {
        return text(text, TextColor.WHITE);
    }

    public static TextTextureExtractor text(String text, TextColor color) {
        return new TextTextureExtractor(Component.literal(text).withColor(color));
    }
}

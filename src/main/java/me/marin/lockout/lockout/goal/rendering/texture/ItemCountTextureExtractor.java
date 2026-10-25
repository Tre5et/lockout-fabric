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

public class ItemCountTextureExtractor implements TextureExtractor {
    public static final MapCodec<ItemCountTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(t -> t.getText().getString()),
            TextColor.CODEC.optionalFieldOf("color", TextColor.WHITE).forGetter(t -> Optional.ofNullable(t.getText().getStyle().getColor()).orElse(TextColor.WHITE))
    ).apply(instance, (t,c) -> new ItemCountTextureExtractor(Component.literal(t).withColor(c))));

    @Getter
    private final Component text;

    public ItemCountTextureExtractor(Component text) {
        this.text = text;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.text(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, -1, true);
        });
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

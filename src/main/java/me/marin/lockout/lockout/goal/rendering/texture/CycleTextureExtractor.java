package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CycleTextureExtractor implements TextureExtractor {
    public static final MapCodec<CycleTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(TextureExtractor.CODEC).fieldOf("textures").forGetter(CycleTextureExtractor::getRenderers)
    ).apply(instance, CycleTextureExtractor::new));

    @Getter
    private final List<TextureExtractor> renderers;

    public CycleTextureExtractor(List<TextureExtractor> renderers) {
        this.renderers = renderers;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        int mod = Math.toIntExact(tick % (60L * renderers.size()));
        renderers.get(mod / 60).extract(extractor, font, x, y, width, height, tick);
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }

    public static CycleTextureExtractor item(List<Item> items) {
        return new CycleTextureExtractor(
                items.stream()
                        .map(ItemTextureExtractor::item)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public static CycleTextureExtractor itemStack(List<Pair<Item, Integer>> items) {
        return new CycleTextureExtractor(
                items.stream()
                        .map(e -> ItemTextureExtractor.stack(e.getA(), e.getB()))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public static CycleTextureExtractor texture(List<Identifier> textures) {
        return new CycleTextureExtractor(
                textures.stream()
                        .map(GenericTextureExtractor::texture)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public static CycleTextureExtractor texture(Identifier... textures) {
        return texture(Arrays.stream(textures).toList());
    }

}

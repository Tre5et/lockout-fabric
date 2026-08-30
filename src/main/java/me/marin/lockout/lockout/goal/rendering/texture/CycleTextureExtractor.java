package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;

public class CycleTextureExtractor implements TextureExtractor {
    private final List<? extends TextureExtractor> renderers;

    public CycleTextureExtractor(List<? extends TextureExtractor> renderers) {
        this.renderers = renderers;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        int mod = Math.toIntExact(tick % (60L * renderers.size()));
        renderers.get(mod / 60).extract(extractor, font, x, y, width, height, tick);
    }

    public static CycleTextureExtractor item(List<Item> items) {
        return new CycleTextureExtractor(
                items.stream()
                        .map(ItemTextureExtractor::item)
                        .toList()
        );
    }

    public static CycleTextureExtractor itemStack(List<Pair<Item, Integer>> items) {
        return new CycleTextureExtractor(
                items.stream()
                        .map(e -> ItemTextureExtractor.stack(e.getA(), e.getB()))
                        .toList()
        );
    }

    public static CycleTextureExtractor texture(List<Identifier> textures) {
        return new CycleTextureExtractor(
                textures.stream()
                        .map(GenericTextureExtractor::texture)
                        .toList()
        );
    }

    public static CycleTextureExtractor texture(Identifier... textures) {
        return texture(Arrays.stream(textures).toList());
    }

}

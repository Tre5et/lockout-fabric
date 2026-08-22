package me.marin.lockout.lockout.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;

public class CycleTextureRenderer implements TextureRenderer {
    private final List<? extends TextureRenderer> renderers;

    private CycleTextureRenderer(List<? extends TextureRenderer> renderers) {
        this.renderers = renderers;
    }

    @Override
    public void renderTexture(GuiGraphicsExtractor extractor, Font font, int x, int y, int tick) {
        int mod = tick % (60 * renderers.size());
        renderers.get(mod / 60).renderTexture(extractor, font, x, y, tick);

    }

    public static CycleTextureRenderer item(List<Item> items) {
        return new CycleTextureRenderer(
                items.stream()
                        .map(ItemTextureRenderer::item)
                        .toList()
        );
    }

    public static CycleTextureRenderer itemStack(List<Pair<Item, Integer>> items) {
        return new CycleTextureRenderer(
                items.stream()
                        .map(e -> ItemTextureRenderer.stack(e.getA(), e.getB()))
                        .toList()
        );
    }

    public static CycleTextureRenderer texture(Identifier... textures) {
        return new CycleTextureRenderer(
                Arrays.stream(textures)
                        .map(GenericTextureRenderer::texture)
                        .toList()
        );
    }

}

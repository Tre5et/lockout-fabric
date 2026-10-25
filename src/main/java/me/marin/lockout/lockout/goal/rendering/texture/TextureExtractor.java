package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Registry;

import java.awt.*;

public interface TextureExtractor {
    Codec<TextureExtractor> CODEC = LockoutRegistries.TEXTURE_EXTRACTOR_TYPE.byNameCodec().dispatch(TextureExtractor::codec, c -> c);

    static void bootstrap(Registry<MapCodec<? extends TextureExtractor>> registry) {
        LockoutRegistries.register(registry, "blank", Blank.MAP_CODEC);
        LockoutRegistries.register(registry, "corner_icon", CornerIconTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "cycle", CycleTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "fill", FillTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "generic", GenericTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "item_count", ItemCountTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "item_decoration", ItemDecorationTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "item", ItemTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "overlay", OverlayTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "sprite", SpriteTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "stack", StackingTextureExtractor.MAP_CODEC);
        LockoutRegistries.register(registry, "text", TextTextureExtractor.MAP_CODEC);
    }

    /**
     * Renders/displays any custom texture. This method is called every client tick, meaning that the texture can change.
     * @param tick Number of client ticks passed. This number increases every client tick.
     */
    void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick);

    MapCodec<? extends TextureExtractor> codec();

    default TextureExtractor overlay(TextureExtractor extractor, TextureAnchor anchor, int width, int height) {
        return new OverlayTextureExtractor(this, extractor, anchor, width, height);
    }

    default TextureExtractor overlay(TextureExtractor extractor, TextureAnchor anchor, int size) {
        return overlay(extractor, anchor, size, size);
    }

    default TextureExtractor overlay(TextureExtractor extractor, TextureAnchor anchor) {
        return overlay(extractor, anchor, 8);
    }

    default void withScale(GuiGraphicsExtractor extractor, int x, int y, float scale, Runnable function) {
        extractor.pose().pushMatrix();
        extractor.pose().scaleAround(scale, x, y);
        function.run();
        extractor.pose().popMatrix();
    }

    static Blank blank() {
        return new Blank();
    }

    class Blank implements TextureExtractor {
        public static MapCodec<Blank> MAP_CODEC = MapCodec.unit(new Blank());

        @Override
        public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {

        }

        @Override
        public MapCodec<? extends TextureExtractor> codec() {
            return MAP_CODEC;
        }
    }
}

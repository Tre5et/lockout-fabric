package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;

public interface TextureExtractorProvider {
    Codec<TextureExtractorProvider> CODEC = TextureExtractor.CODEC.xmap(e -> _ -> e, p -> p.get(null));

    TextureExtractor get(Object option);
}

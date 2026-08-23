package me.marin.lockout.lockout.goal.rendering.texture;

public interface TextureExtractorProvider<T> {
    TextureExtractor get(T option);
}

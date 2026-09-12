package me.marin.lockout.lockout.goal.rendering.texture;

import oshi.util.tuples.Pair;

public enum TextureAnchor {
    TOP_LEFT(TextureAnchorTransformer.START, TextureAnchorTransformer.START),
    TOP_CENTER(TextureAnchorTransformer.CENTER, TextureAnchorTransformer.START),
    TOP_RIGHT(TextureAnchorTransformer.END, TextureAnchorTransformer.START),
    CENTER_LEFT(TextureAnchorTransformer.START, TextureAnchorTransformer.CENTER),
    CENTER_CENTER(TextureAnchorTransformer.CENTER, TextureAnchorTransformer.CENTER),
    CENTER_RIGHT(TextureAnchorTransformer.END, TextureAnchorTransformer.CENTER),
    BOTTOM_LEFT(TextureAnchorTransformer.START, TextureAnchorTransformer.END),
    BOTTOM_CENTER(TextureAnchorTransformer.CENTER, TextureAnchorTransformer.END),
    BOTTOM_RIGHT(TextureAnchorTransformer.END, TextureAnchorTransformer.END);

    private final TextureAnchorTransformer xTransformer;
    private final TextureAnchorTransformer yTransformer;

    TextureAnchor(TextureAnchorTransformer xTransformer, TextureAnchorTransformer yTransformer) {
        this.xTransformer = xTransformer;
        this.yTransformer = yTransformer;
    }

    public Pair<Integer, Integer> getAnchor(int x, int y, int width, int height, int targetWidth, int targetHeight) {
        return new Pair<>(
                x + xTransformer.transform(width, targetWidth),
                y + yTransformer.transform(height, targetHeight)
        );
    }

    private interface TextureAnchorTransformer {
        int transform(int totalSize, int targetSize);

        TextureAnchorTransformer START = (_, _) -> 0;
        TextureAnchorTransformer CENTER = (total,target) -> (total/2) - (target/2);
        TextureAnchorTransformer END = (total,target) -> total - target;
    }
}

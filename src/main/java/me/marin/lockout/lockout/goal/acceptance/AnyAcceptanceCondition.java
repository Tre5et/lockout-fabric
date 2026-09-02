package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;

import java.util.List;
import java.util.function.Supplier;

public class AnyAcceptanceCondition<T> implements AcceptanceCondition<T> {
    private final String id;
    private final Supplier<String> nameSupplier;
    private final Supplier<List<TextureExtractor>> textureExtractorSupplier;

    public AnyAcceptanceCondition(String id, Supplier<String> nameSupplier, Supplier<List<TextureExtractor>> textureExtractorSupplier) {
        this.id = id;
        this.nameSupplier = nameSupplier;
        this.textureExtractorSupplier = textureExtractorSupplier;
    }

    @Override
    public boolean test(T value) {
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return nameSupplier.get();
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return textureExtractorSupplier.get();
    }
}

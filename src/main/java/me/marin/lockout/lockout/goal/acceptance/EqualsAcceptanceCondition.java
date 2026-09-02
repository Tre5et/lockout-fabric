package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class EqualsAcceptanceCondition<T,E> implements AcceptanceCondition<T> {
    private final E value;
    private final Supplier<String> idSupplier;
    private final Supplier<String> nameSupplier;
    private final Supplier<TextureExtractor> textureExtractorSupplier;
    private final BiPredicate<T,E> equalsFunction;

    public EqualsAcceptanceCondition(E value, Supplier<String> idSupplier, Supplier<String> nameSupplier, Supplier<TextureExtractor> textureExtractorSupplier, BiPredicate<T, E> equalsFunction) {
        this.value = value;
        this.idSupplier = idSupplier;
        this.nameSupplier = nameSupplier;
        this.textureExtractorSupplier = textureExtractorSupplier;
        this.equalsFunction = equalsFunction;
    }

    public EqualsAcceptanceCondition(E value, Supplier<String> idSupplier, Supplier<String> nameSupplier, Supplier<TextureExtractor> textureExtractorSupplier) {
        this(value, idSupplier, nameSupplier, textureExtractorSupplier, Objects::equals);
    }

    @Override
    public boolean test(T value) {
        return equalsFunction.test(value, this.value);
    }

    @Override
    public String getId() {
        return idSupplier.get();
    }

    @Override
    public String getName() {
        return nameSupplier.get();
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return List.of(textureExtractorSupplier.get());
    }

    public <M> EqualsAcceptanceCondition<M,E> mapEquals(BiPredicate<M,E> equalsFunction) {
        return new EqualsAcceptanceCondition<>(value, idSupplier, nameSupplier, textureExtractorSupplier, equalsFunction);
    }

    public static EqualsAcceptanceCondition<ResourceKey<DamageType>, ResourceKey<DamageType>> damageType(ResourceKey<DamageType> type, Supplier<TextureExtractor> textureExtractorSupplier) {
        return new EqualsAcceptanceCondition<>(
                type, () -> BuilderUtil.identifierToId(type.identifier()), () -> BuilderUtil.identifierToName(type.identifier()), textureExtractorSupplier
        );
    }
}

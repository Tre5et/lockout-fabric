package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface AcceptanceCondition<T> {
    boolean test(T value);

    String getId();

    String getName();

    List<TextureExtractor> getExamples();

    default AndAcceptanceCondition<T> and(AcceptanceCondition<T> condition) {
        return new AndAcceptanceCondition<>(this, condition);
    }

    default <M> MappedAcceptanceCondition<T,M> map(Function<M,T> mapper) {
        return new MappedAcceptanceCondition<>(this, mapper);
    }

    class MappedAcceptanceCondition<T,M> implements AcceptanceCondition<M> {
        private final AcceptanceCondition<T> original;
        private final Function<M,T> mapper;

        public MappedAcceptanceCondition(AcceptanceCondition<T> original, Function<M, T> mapper) {
            this.original = original;
            this.mapper = mapper;
        }

        @Override
        public boolean test(M value) {
            return original.test(mapper.apply(value));
        }

        @Override
        public String getId() {
            return original.getId();
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public List<TextureExtractor> getExamples() {
            return original.getExamples();
        }
    }

    class AndAcceptanceCondition<T> implements AcceptanceCondition<T> {
        private final AcceptanceCondition<T> a;
        private final AcceptanceCondition<T> b;

        public AndAcceptanceCondition(AcceptanceCondition<T> a, AcceptanceCondition<T> b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean test(T value) {
            return a.test(value) && b.test(value);
        }

        @Override
        public String getId() {
            return a.getId() + "_AND_" + b.getId();
        }

        @Override
        public String getName() {
            return a.getName() + " " + b.getName();
        }

        @Override
        public List<TextureExtractor> getExamples() {
            List<TextureExtractor> extractors = new ArrayList<>();
            extractors.addAll(a.getExamples());
            extractors.addAll(b.getExamples());
            return extractors;
        }
    }
}

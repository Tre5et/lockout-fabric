package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;

import java.util.List;
import java.util.function.Function;

public interface AcceptanceCondition<T> {
    boolean test(T value);

    String getId();

    String getName();

    List<TextureExtractor> getExamples();

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
}

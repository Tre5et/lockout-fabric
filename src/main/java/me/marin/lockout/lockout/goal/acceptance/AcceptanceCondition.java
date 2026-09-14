package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface AcceptanceCondition<T> {
    boolean test(T value, ServerPlayer player);

    String getId();

    String getName();

    List<TextureExtractor> getExamples();

    default <M> MappedAcceptanceCondition<T,M> map(Function<M,T> mapper) {
        return new MappedAcceptanceCondition<>(this, mapper);
    }

    default PlayerRequirementAcceptanceCondition<T> withPlayerRequirement(Predicate<ServerPlayer> playerRequirement) {
        return new PlayerRequirementAcceptanceCondition<>(this, playerRequirement);
    }

    default AndAcceptanceCondition<T> and(AcceptanceCondition<T> condition) {
        return new AndAcceptanceCondition<>(this, condition);
    }

    class MappedAcceptanceCondition<T,M> implements AcceptanceCondition<M> {
        private final AcceptanceCondition<T> original;
        private final Function<M,T> mapper;

        public MappedAcceptanceCondition(AcceptanceCondition<T> original, Function<M, T> mapper) {
            this.original = original;
            this.mapper = mapper;
        }

        @Override
        public boolean test(M value, ServerPlayer player) {
            return original.test(mapper.apply(value), player);
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

    class PlayerRequirementAcceptanceCondition<T> implements AcceptanceCondition<T> {
        private final AcceptanceCondition<T> original;
        private final Predicate<ServerPlayer> playerPredicate;

        public PlayerRequirementAcceptanceCondition(AcceptanceCondition<T> original, Predicate<ServerPlayer> playerPredicate) {
            this.original = original;
            this.playerPredicate = playerPredicate;
        }

        @Override
        public boolean test(T value, ServerPlayer player) {
            return original.test(value, player) && playerPredicate.test(player);
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
        public boolean test(T value, ServerPlayer player) {
            return a.test(value, player) && b.test(value, player);
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

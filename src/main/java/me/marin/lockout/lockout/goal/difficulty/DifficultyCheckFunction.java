package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public abstract class DifficultyCheckFunction {
    public static final Codec<DifficultyCheckFunction> CODEC = LockoutRegistries.DIFFICULTY_CHECK_TYPE.byNameCodec().dispatch(DifficultyCheckFunction::codec, c -> c);

    private static final Random RANDOM = new Random();

    private final float target;
    private final Function<Float,Float> probabilityFunction;
    @Getter
    private final Optional<List<Modifier>> modifiers;

    public abstract MapCodec<? extends DifficultyCheckFunction> codec();

    public static Registry<MapCodec<? extends DifficultyCheckFunction>> bootstrap(Registry<MapCodec<? extends DifficultyCheckFunction>> registry) {
        LockoutRegistries.register(registry, "allow_all", AllowAll.MAP_CODEC);
        LockoutRegistries.register(registry, "absolute_distance", AbsoluteDistance.MAP_CODEC);
        return registry;
    }

    public DifficultyCheckFunction(float target, Function<Float, Float> probabilityFunction, Optional<List<Modifier>> modifiers) {
        this.target = target;
        this.probabilityFunction = probabilityFunction;
        this.modifiers = modifiers;
    }

    public DifficultyStatus check(float difficulty) {
        float probability = this.probabilityFunction.apply(difficulty);
        if(modifiers.isPresent()) {
            for (Modifier m : modifiers.get()) {
                probability = m.apply(difficulty, probability);
            }
        }
        if(probability > RANDOM.nextFloat()) return DifficultyStatus.ACCEPTED;
        if(difficulty > target) return DifficultyStatus.TOO_HARD;
        return DifficultyStatus.TOO_EASY;
    }

/*    public DifficultyCheckFunction scale(float factor) {
        return new DifficultyCheckFunction(this.target, d -> Math.clamp(this.probabilityFunction.apply(d) * factor, 0, 1));
    }

    public DifficultyCheckFunction allowRange(float min, float max) {
        return new DifficultyCheckFunction(this.target, d -> (d >= min && d <= max) ? this.probabilityFunction.apply(d) : 0);
    }*/

    public static class AllowAll extends DifficultyCheckFunction {
        public static final MapCodec<AllowAll> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(Modifier.CODEC).optionalFieldOf("modifiers").forGetter(AllowAll::getModifiers)
        ).apply(instance, AllowAll::new));

        public AllowAll(Optional<List<Modifier>> modifiers) {
            super(0.5f, _ -> 1f, modifiers);
        }

        @Override
        public MapCodec<? extends DifficultyCheckFunction> codec() {
            return MAP_CODEC;
        }
    }

    public static class AbsoluteDistance extends DifficultyCheckFunction {
        @Getter
        private final float target;
        @Getter
        private final float steepness;
        @Getter
        private final Optional<Float> exponent;

        public static final MapCodec<AbsoluteDistance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("target").forGetter(AbsoluteDistance::getTarget),
                Codec.FLOAT.fieldOf("steepness").forGetter(AbsoluteDistance::getSteepness),
                Codec.FLOAT.optionalFieldOf("exponent").forGetter(AbsoluteDistance::getExponent),
                Codec.list(Modifier.CODEC).optionalFieldOf("modifiers").forGetter(AbsoluteDistance::getModifiers)
        ).apply(instance, AbsoluteDistance::new));

        public AbsoluteDistance(float target, float steepness, Optional<Float> exponent, Optional<List<Modifier>> modifiers) {
            super(target, d -> (float)Math.clamp(1 - Math.pow(Math.abs(target - d) * steepness, exponent.orElse(1f)), 0, 1), modifiers);
            this.target = target;
            this.steepness = steepness;
            this.exponent = exponent;
        }

        @Override
        public MapCodec<? extends DifficultyCheckFunction> codec() {
            return MAP_CODEC;
        }
    }

    public static abstract class Modifier {
        public static final Codec<Modifier> CODEC = LockoutRegistries.DIFFICULTY_CHECK_MODIFIER_TYPE.byNameCodec().dispatch(Modifier::codec, c -> c);

        public static Registry<MapCodec<? extends Modifier>> bootstrap(Registry<MapCodec<? extends Modifier>> registry) {
            LockoutRegistries.register(registry, "scale", Scale.MAP_CODEC);
            LockoutRegistries.register(registry, "allow_between", AllowBetween.MAP_CODEC);
            return registry;
        }

        public abstract float apply(float in, float out);
        public abstract MapCodec<? extends Modifier> codec();

        public static class Scale extends Modifier {
            public static final MapCodec<Scale> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(Scale::getFactor)
            ).apply(instance, Scale::new));

            @Getter
            private final float factor;

            public Scale(float factor) {
                this.factor = factor;
            }

            @Override
            public float apply(float in, float out) {
                return Math.clamp(out * factor, 0, 1);
            }

            @Override
            public MapCodec<? extends Modifier> codec() {
                return MAP_CODEC;
            }
        }

        public static class AllowBetween extends Modifier {
            public static final MapCodec<AllowBetween> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("min").forGetter(AllowBetween::getMin),
                    Codec.FLOAT.fieldOf("max").forGetter(AllowBetween::getMax)
            ).apply(instance, AllowBetween::new));

            @Getter
            private final float min;
            @Getter
            private final float max;

            public AllowBetween(float min, float max) {
                this.min = min;
                this.max = max;
            }

            @Override
            public float apply(float in, float out) {
                return (in >= min && in <= max) ? out : 0;
            }

            @Override
            public MapCodec<? extends Modifier> codec() {
                return MAP_CODEC;
            }
        }
    }

    public enum DifficultyStatus {
        TOO_EASY(false),
        ACCEPTED(true),
        TOO_HARD(false);

        @Getter
        private final boolean allowed;

        DifficultyStatus(boolean allowed) {
            this.allowed = allowed;
        }
    }
}

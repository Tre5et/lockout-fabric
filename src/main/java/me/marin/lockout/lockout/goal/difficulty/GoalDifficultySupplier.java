package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;

public interface GoalDifficultySupplier<T> {
    Codec<GoalDifficultySupplier<?>> CODEC = LockoutRegistries.DIFFICULTY_SUPPLIER_TYPE.byNameCodec().dispatch(GoalDifficultySupplier::codec, c -> c);

    float get(T option);

    MapCodec<? extends GoalDifficultySupplier<?>> codec();

    static <T> Simple<T> of(Number difficulty) {
        return new Simple<>(difficulty.floatValue());
    }

    static <T extends Number> Linear<T> linear(Number minIn, Number maxIn) {
        return new Linear<>(minIn.floatValue(), maxIn.floatValue(), 0f, 1f);
    }

    static void bootstrap(Registry<MapCodec<? extends GoalDifficultySupplier<?>>> registry) {
        LockoutRegistries.register(registry, "simple", Simple.MAP_CODEC);
        LockoutRegistries.register(registry, "linear", Linear.MAP_CODEC);
    }

    record Simple<T>(float difficulty) implements GoalDifficultySupplier<T> {
        public static final MapCodec<Simple<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("difficulty").forGetter(Simple::difficulty)
        ).apply(instance, Simple::new));

        @Override
        public float get(T option) {
            return difficulty;
        }

        @Override
        public MapCodec<? extends GoalDifficultySupplier<?>> codec() {
            return MAP_CODEC;
        }
    }

    record Linear<T extends Number>(float min, float max, float start, float end) implements GoalDifficultySupplier<T> {
        public static final MapCodec<Linear<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("min").forGetter(Linear::min),
                Codec.FLOAT.fieldOf("max").forGetter(Linear::max),
                Codec.FLOAT.optionalFieldOf("start", 0f).forGetter(Linear::start),
                Codec.FLOAT.optionalFieldOf("end", 1f).forGetter(Linear::end)
        ).apply(instance, Linear::new));

        @Override
        public float get(T option) {
            return ((option.floatValue() - min) / (max - min)) * (end - start) + start;
        }

        @Override
        public MapCodec<? extends GoalDifficultySupplier<?>> codec() {
            return MAP_CODEC;
        }

        public Linear<T> inRange(float start, float end) {
            return new Linear<>(min, max, start, end);
        }
    }
}

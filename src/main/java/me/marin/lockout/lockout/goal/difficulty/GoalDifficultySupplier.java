package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;

import java.util.Optional;

public interface GoalDifficultySupplier<T> {
    Codec<GoalDifficultySupplier<?>> CODEC = Codec.withAlternative(
            LockoutRegistries.DIFFICULTY_SUPPLIER_VOID_TYPE.byNameCodec().dispatch(GoalDifficultySupplier::codec, c -> c),
            Codec.floatRange(0f,1f).<GoalDifficultySupplier<Object>>xmap(GoalDifficultySupplier::of, d -> d.getInternal(null))
    );

    float getInternal(T option);

    float defaultValue();

    Optional<T> toType(Object option);

    default float get(Object option) {
        Optional<T> type = toType(option);
        return type.map(this::getInternal).orElseGet(this::defaultValue);
    }

    MapCodec<? extends GoalDifficultySupplier<? super Number>> codec();

    static Simple of(Number difficulty) {
        return new Simple(difficulty.floatValue());
    }

    static Linear linear(Number minIn, Number maxIn) {
        return new Linear(minIn.floatValue(), maxIn.floatValue(), 0f, 1f);
    }

    static void bootstrap(Registry<MapCodec<? extends GoalDifficultySupplier<? super Number>>> registry) {
        LockoutRegistries.register(registry, "simple", Simple.MAP_CODEC);
        LockoutRegistries.register(registry, "linear", Linear.MAP_CODEC);
    }

    record Simple(float difficulty) implements GoalDifficultySupplier<Object> {
        public static final MapCodec<Simple> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("difficulty").forGetter(Simple::difficulty)
        ).apply(instance, Simple::new));

        @Override
        public float getInternal(Object option) {
            return difficulty;
        }

        @Override
        public float defaultValue() {
            return difficulty;
        }

        @Override
        public Optional<Object> toType(Object option) {
            return Optional.ofNullable(option);
        }

        @Override
        public MapCodec<? extends GoalDifficultySupplier<? super Number>> codec() {
            return MAP_CODEC;
        }
    }

    record Linear(float min, float max, float start, float end) implements GoalDifficultySupplier<Number> {
        public static final MapCodec<Linear> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("min").forGetter(Linear::min),
                Codec.FLOAT.fieldOf("max").forGetter(Linear::max),
                Codec.FLOAT.optionalFieldOf("start", 0f).forGetter(Linear::start),
                Codec.FLOAT.optionalFieldOf("end", 1f).forGetter(Linear::end)
        ).apply(instance, Linear::new));

        @Override
        public float getInternal(Number option) {
            return ((option.floatValue() - min) / (max - min)) * (end - start) + start;
        }

        @Override
        public float defaultValue() {
            return max;
        }

        @Override
        public Optional<Number> toType(Object option) {
            if(!(option instanceof Number number)) return Optional.empty();
            return Optional.of(number);
        }

        @Override
        public MapCodec<? extends GoalDifficultySupplier<? super Number>> codec() {
            return MAP_CODEC;
        }

        public Linear inRange(float start, float end) {
            return new Linear(min, max, start, end);
        }
    }
}

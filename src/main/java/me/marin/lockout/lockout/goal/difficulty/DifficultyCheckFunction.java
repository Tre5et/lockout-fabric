package me.marin.lockout.lockout.goal.difficulty;

import lombok.Getter;

import java.util.Random;
import java.util.function.Function;

public class DifficultyCheckFunction {
    private static final Random RANDOM = new Random();

    private final float target;
    private final Function<Float,Float> probabilityFunction;

    public DifficultyCheckFunction(float target, Function<Float, Float> probabilityFunction) {
        this.target = target;
        this.probabilityFunction = probabilityFunction;
    }

    public DifficultyStatus checkDifficulty(float difficulty) {
        if(this.probabilityFunction.apply(difficulty) > RANDOM.nextFloat()) return DifficultyStatus.ACCEPTED;
        if(difficulty > target) return DifficultyStatus.TOO_HARD;
        return DifficultyStatus.TOO_EASY;
    }

    public DifficultyCheckFunction scale(float factor) {
        return new DifficultyCheckFunction(this.target, d -> Math.clamp(this.probabilityFunction.apply(d) * factor, 0, 1));
    }

    public DifficultyCheckFunction allowRange(float min, float max) {
        return new DifficultyCheckFunction(this.target, d -> (d >= min && d <= max) ? this.probabilityFunction.apply(d) : 0);
    }

    static DifficultyCheckFunction random() {
        return new DifficultyCheckFunction(0.5f, _ -> 1f);
    }

    static DifficultyCheckFunction absoluteDistance(float target, float steepness) {
        return new DifficultyCheckFunction(target, d -> Math.clamp(1 - Math.abs(target - d) * steepness, 0, 1));
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

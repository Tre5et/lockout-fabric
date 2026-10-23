package me.marin.lockout.lockout.goal.difficulty;

public interface GoalDifficultySupplier<T> {
    float get(T option);

    default GoalDifficultySupplier<T> scale(Number min, Number max) {
        return d -> this.get(d) * (max.floatValue() - min.floatValue()) + min.floatValue();
    }

    static <T> GoalDifficultySupplier<T> of(Number difficulty) {
        return _ -> difficulty.floatValue();
    }

    static GoalDifficultySupplier<Number> linear(Number minIn, Number maxIn) {
        return d -> (d.floatValue() - minIn.floatValue()) / (maxIn.floatValue() - minIn.floatValue());
    }
}

package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetFloatGoalProgress;

import java.util.function.Function;

public class TargetFloatServerGoalProgress<T> extends TargetFloatGoalProgress implements ServerGoalProgress<T, Float> {
    private final Function<T,Float> updateFunction;

    public TargetFloatServerGoalProgress(Float target, Function<T, Float> updateFunction) {
        super(target);
        this.updateFunction = updateFunction;
    }

    @Override
    public Float update(Float current, T update) {
        return current + updateFunction.apply(update);
    }
}

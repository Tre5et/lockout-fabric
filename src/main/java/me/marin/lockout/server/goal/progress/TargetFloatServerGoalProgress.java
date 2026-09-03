package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetFloatGoalProgress;

import java.util.function.Function;

public class TargetFloatServerGoalProgress<T> extends TargetFloatGoalProgress implements ServerGoalProgress<T, Number> {
    private final Function<T,Number> updateFunction;

    public TargetFloatServerGoalProgress(Number target, Function<T, Number> updateFunction) {
        super(target);
        this.updateFunction = updateFunction;
    }

    @Override
    public Number update(Number current, T update) {
        return current.doubleValue() + updateFunction.apply(update).doubleValue();
    }
}

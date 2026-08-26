package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import me.marin.lockout.lockout.goal.progress.GoalProgressTracker;
import me.marin.lockout.lockout.goal.progress.TargetIntegerProgressTracker;

import java.util.function.Function;

public class CountingProgressGoal<T> extends ProgressingGoal<T,Integer,Integer,TargetIntegerProgressTracker> {
    private final Function<T, Integer> countUpStep;

    public CountingProgressGoal(GoalBuildParameters parameters, String title, int target, Function<T,Integer> countUpStep) {
        super(parameters, GoalProgressTracker.integer(title, target), v -> v >= target);
        this.countUpStep = countUpStep;
    }

    @Override
    protected Integer getUpdateData(T data, Integer progress) {
        return countUpStep.apply(data);
    }
}

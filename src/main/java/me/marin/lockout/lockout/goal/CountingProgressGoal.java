package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.progress.GoalProgressTracker;
import me.marin.lockout.lockout.goal.progress.TargetIntegerProgressTracker;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Function;

public class CountingProgressGoal<T> extends ProgressingGoal<T,Integer,Integer,TargetIntegerProgressTracker> {
    private final Function<T, Integer> countUpStep;

    public CountingProgressGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, List<GoalHint> hints, Pair<String, String> buildData, String title, int target, Function<T,Integer> countUpStep) {
        super(id, nameExtractor, textureExtractor, hints, buildData, GoalProgressTracker.integer(title, target), v -> v >= target);
        this.countUpStep = countUpStep;
    }

    @Override
    protected Integer getUpdateData(T data, Integer progress) {
        return countUpStep.apply(data);
    }
}

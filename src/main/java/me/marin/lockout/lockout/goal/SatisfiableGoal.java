package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Predicate;

public abstract class SatisfiableGoal<T> extends Goal {
    private final Predicate<T> satisfiedPredicate;

    public SatisfiableGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, List<GoalHint> hints, Pair<String, String> buildData, Predicate<T> satisfiedPredicate) {
        super(id, nameExtractor, textureExtractor, hints, buildData);
        this.satisfiedPredicate = satisfiedPredicate;
    }

    public boolean satisfiedBy(T data) {
        return satisfiedPredicate.test(data);
    }
}

package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.texture.TextureRenderer;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Predicate;

public abstract class SatisfiableGoal<T> extends Goal {
    private final Predicate<T> satisfiedPredicate;

    public SatisfiableGoal(String id, String name, TooltipInfo tooltipInfo, TextureRenderer textureRenderer, List<GoalHint> hints, Pair<String, String> buildData, Predicate<T> satisfiedPredicate) {
        super(id, name, tooltipInfo, textureRenderer, hints, buildData);
        this.satisfiedPredicate = satisfiedPredicate;
    }

    public boolean satisfiedBy(T data) {
        return satisfiedPredicate.test(data);
    }
}

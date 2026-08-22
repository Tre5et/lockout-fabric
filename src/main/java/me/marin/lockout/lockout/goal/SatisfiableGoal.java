package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.texture.TextureRenderer;
import oshi.util.tuples.Pair;

import java.util.function.Predicate;

public abstract class SatisfiableGoal<T> extends Goal {
    private final Predicate<T> satisfiedPredicate;

    public SatisfiableGoal(String id, String name, TooltipInfo tooltipInfo, TextureRenderer textureRenderer, Pair<String, String> buildData, Predicate<T> satisfiedPredicate) {
        super(id, name, tooltipInfo, textureRenderer, buildData);
        this.satisfiedPredicate = satisfiedPredicate;
    }

    public boolean satisfiedBy(T data) {
        return satisfiedPredicate.test(data);
    }
}

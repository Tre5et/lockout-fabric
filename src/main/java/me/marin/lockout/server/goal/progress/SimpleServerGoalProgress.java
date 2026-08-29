package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.SimpleGoalProgress;

import java.util.function.Predicate;

public class SimpleServerGoalProgress<U> extends SimpleGoalProgress implements ServerGoalProgress<U,Boolean> {
    private final Predicate<U> satisfiedPredicate;

    public SimpleServerGoalProgress(Predicate<U> satisfiedPredicate) {
        this.satisfiedPredicate = satisfiedPredicate;
    }

    @Override
    public Boolean update(Boolean current, U update) {
        return current || satisfiedPredicate.test(update);
    }
}

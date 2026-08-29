package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetNumberGoalProgress;

import java.util.function.Predicate;

public class ServerTargetNumberGoalProgress<T> extends TargetNumberGoalProgress implements ServerGoalProgress<T,Integer> {
    private final Predicate<T> updatePredicate;

    public ServerTargetNumberGoalProgress(int target, Predicate<T> updatePredicate) {
        super(target);
        this.updatePredicate = updatePredicate;
    }

    @Override
    public Integer update(Integer current, T update) {
        if(updatePredicate.test(update)) {
            return current + 1;
        }
        return current;
    }
}

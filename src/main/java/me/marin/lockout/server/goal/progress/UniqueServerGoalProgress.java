package me.marin.lockout.server.goal.progress;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class UniqueServerGoalProgress<T> extends TargetNumberServerGoalProgress<T> {
    private final Set<T> values = new HashSet<>();

    public UniqueServerGoalProgress(int target, Predicate<T> updatePredicate) {
        super(target, updatePredicate);
    }

    @Override
    public Integer update(Integer current, T update) {
        if(!values.contains(update)) {
            values.add(update);
            return super.update(current, update);
        }
        return current;
    }
}

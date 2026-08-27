package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class SatisfiableGoal<T> extends Goal<T> {
    private final Predicate<T> satisfiedPredicate;

    public SatisfiableGoal(GoalBuildParameters parameters, Predicate<T> satisfiedPredicate) {
        super(parameters);
        this.satisfiedPredicate = satisfiedPredicate;
    }

    @Override
    public void updateWith(T data, ServerPlayer player) {
        if(satisfiedPredicate.test(data)) {
            complete(player, true);
        }
    }
}

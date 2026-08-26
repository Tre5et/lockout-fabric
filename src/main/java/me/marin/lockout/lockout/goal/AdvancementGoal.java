package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import net.minecraft.advancements.AdvancementHolder;

import java.util.function.Predicate;

public class AdvancementGoal extends SatisfiableGoal<AdvancementHolder> {
    public AdvancementGoal(GoalBuildParameters parameters, Predicate<AdvancementHolder> satisfiedPredicate) {
        super(parameters, satisfiedPredicate);
    }
}

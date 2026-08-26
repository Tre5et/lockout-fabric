package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import net.minecraft.advancements.AdvancementHolder;

import java.util.function.Function;

public class AdvancementCountingGoal extends CountingProgressGoal<AdvancementHolder> {
    public AdvancementCountingGoal(GoalBuildParameters parameters, String title, int target, Function<AdvancementHolder, Integer> countUpStep) {
        super(parameters, title, target, a -> a.id().getPath().startsWith("recipe") ? null : countUpStep.apply(a));
    }
}

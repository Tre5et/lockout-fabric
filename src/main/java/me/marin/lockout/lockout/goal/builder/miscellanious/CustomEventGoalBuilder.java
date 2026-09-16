package me.marin.lockout.lockout.goal.builder.miscellanious;

import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;

public class CustomEventGoalBuilder<T> extends GoalBuilder<CustomEvent, T> {
    public CustomEventGoalBuilder(GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, CustomEvent, ?> progressSupplier) {
        super("EVENT", "", category, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(CustomEvent update) {}

    public static CustomEventGoalBuilder<Void> of(GoalCategory category, CustomEvent event) {
        return new CustomEventGoalBuilder<>(
                category,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> event.getAcceptanceCondition())
        );
    }

    public static CustomEventGoalBuilder<Void> of(CustomEvent event) {
        return of(GoalCategory.MISC_ACTIONS, event);
    }
}

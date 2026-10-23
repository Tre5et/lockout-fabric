package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConsumeItemGoalBuilder<T> extends GoalBuilder<ItemUtil.ConsumedItem, T> {
    public ConsumeItemGoalBuilder( GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier, GoalDifficultySupplier<T> difficultySupplier) {
        super("CONSUME", "Consume", GoalCategory.EATING_DRINKING, optionSupplier, progressSupplier.map(ItemUtil.ConsumedItem::itemStack), difficultySupplier);
    }

    @Override
    public void reifiedUpdater(ItemUtil.ConsumedItem update) {}

    public static ConsumeItemGoalBuilder<Void> any(GoalDifficultySupplier<Void> difficultySupplier, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items)),
                difficultySupplier
        );
    }

    public static <T> ConsumeItemGoalBuilder<Void> anyWithComponent(GoalDifficultySupplier<Void> difficultySupplier, DataComponentType<T> component, T value, String id, String name, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> ItemWithComponentAcceptanceCondition.single(component, value, id, name, items)),
                difficultySupplier
        );
    }

    public static ConsumeItemGoalBuilder<Integer> unique(GoalDifficultySupplier<Integer> difficultySupplier, int min, int max, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to consume", min, max, 1),
                GoalProgressSupplier.unique("Items consumed", _ -> InListAcceptanceCondition.item(items), ItemStack::getItem),
                difficultySupplier
        );
    }

    public static ConsumeItemGoalBuilder<Integer> uniqueWithComponent(GoalDifficultySupplier<Integer> difficultySupplier, int min, int max, DataComponentType<?>... components) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to consume", min, max, 1),
                GoalProgressSupplier.unique("Items consumed", _ -> ItemWithComponentAcceptanceCondition.hasComponents(components), ItemStack::getItem),
                difficultySupplier
        );
    }

    public static ConsumeItemGoalBuilder<Void> whileHavingEffect(GoalDifficultySupplier<Void> difficultySupplier, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items).withPlayerRequirement(p -> !p.getActiveEffects().isEmpty())),
                difficultySupplier
        );
    }
}

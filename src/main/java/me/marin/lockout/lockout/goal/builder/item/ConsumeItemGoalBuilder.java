package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConsumeItemGoalBuilder<T> extends GoalBuilder<ItemUtil.ConsumedItem, T> {
    public ConsumeItemGoalBuilder( GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("CONSUME", "Consume", GoalCategory.EATING_DRINKING, optionSupplier, progressSupplier.map(ItemUtil.ConsumedItem::itemStack));
    }

    public static ConsumeItemGoalBuilder<Void> any(Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items))
        );
    }

    public static <T> ConsumeItemGoalBuilder<Void> anyWithComponent(DataComponentType<T> component, T value, String id, String name, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> ItemWithComponentAcceptanceCondition.single(component, value, id, name, items))
        );
    }

    public static ConsumeItemGoalBuilder<Integer> unique(int min, int max, Item... items) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to consume", min, max, 1),
                GoalProgressSupplier.unique("Items consumed", _ -> InListAcceptanceCondition.item(items), ItemStack::getItem)
        );
    }

    public static ConsumeItemGoalBuilder<Integer> uniqueWithComponent(int min, int max, DataComponentType<?>... components) {
        return new ConsumeItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to consume", min, max, 1),
                GoalProgressSupplier.unique("Items consumed", _ -> ItemWithComponentAcceptanceCondition.hasComponents(components), ItemStack::getItem)
        );
    }

    @Override
    public void reifiedUpdater(ItemUtil.ConsumedItem update) {}
}

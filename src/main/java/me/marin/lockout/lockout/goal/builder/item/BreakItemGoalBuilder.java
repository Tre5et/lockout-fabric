package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BreakItemGoalBuilder<T> extends GoalBuilder<ItemUtil.BrokenItem, T> {
    public BreakItemGoalBuilder(GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("BREAK", "Break", category, optionSupplier, progressSupplier.map(i -> i.item().getDefaultInstance()));
    }

    public static BreakItemGoalBuilder<Void> withComponent(GoalCategory category, DataComponentType<?>... components) {
        List<DataComponentType<?>> finalComponents = new ArrayList<>(Arrays.stream(components).toList());
        finalComponents.add(DataComponents.MAX_DAMAGE);
        return new BreakItemGoalBuilder<>(
                category,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> ItemWithComponentAcceptanceCondition.hasComponents(finalComponents.toArray(DataComponentType[]::new)).applyAdditional(s -> {
                    if(s.has(DataComponents.MAX_DAMAGE)) {
                        int target = (int)(s.get(DataComponents.MAX_DAMAGE) * 0.85f);
                        s.set(DataComponents.DAMAGE, target);
                    }
                }))
        );
    }

    @Override
    public void reifiedUpdater(ItemUtil.BrokenItem update) {}
}

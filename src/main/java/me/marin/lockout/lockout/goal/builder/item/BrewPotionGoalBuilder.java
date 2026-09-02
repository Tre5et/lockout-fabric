package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

public class BrewPotionGoalBuilder<T> extends GoalBuilder<ItemUtil.BrewedItem, T> {

    public BrewPotionGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("BREW", "Brew", GoalCategory.BREWING, optionSupplier, progressSupplier.map(ItemUtil.BrewedItem::itemStack));
    }

    @SafeVarargs
    public static BrewPotionGoalBuilder<Void> effect(Holder<Potion>... effects) {
        return new BrewPotionGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.potionEffect(effects))
        );
    }

    public static BrewPotionGoalBuilder<Void> type(Item... items) {
        return new BrewPotionGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items))
        );
    }

    @Override
    public void reifiedUpdater(ItemUtil.BrewedItem update) {}
}

package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;

public class CraftItemGoalBuilder<T> extends GoalBuilder<ItemUtil.CraftedItem, T> {
    public CraftItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("CRAFT", "Craft", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier.map(ItemUtil.CraftedItem::itemStack));
    }

    @Override
    public void reifiedUpdater(ItemUtil.CraftedItem update) {}

    public static CraftItemGoalBuilder<Integer> unique(int min, int max, int step) {
        return new CraftItemGoalBuilder<>(
                GoalOptionSupplier.integer("Items to craft", min, max, step),
                GoalProgressSupplier.unique("Items crafted", _ -> new AnyAcceptanceCondition<>("ITEMS", () -> "Items", () -> Collections.singletonList(ItemTextureExtractor.item(Items.CRAFTING_TABLE))), ItemStack::getItem)
        );
    }
}

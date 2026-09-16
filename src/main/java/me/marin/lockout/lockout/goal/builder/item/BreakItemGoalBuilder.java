package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class BreakItemGoalBuilder<T> extends GoalBuilder<ItemUtil.BrokenItem, T> {
    public BreakItemGoalBuilder(GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("BREAK", "Break", category, optionSupplier, progressSupplier.map(i -> i.item().getDefaultInstance()));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        ItemStack stack = Items.GOLDEN_SWORD.getDefaultInstance();
        stack.set(DataComponents.DAMAGE, 27);
        return new StackingTextureExtractor(List.of(textureExtractor, new ItemDecorationTextureExtractor(stack)), 0);
    }

    public static BreakItemGoalBuilder<Void> any(Item... items) {
        return new BreakItemGoalBuilder<>(
                GoalCategory.MISC_ACTIONS,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items))
        );
    }

    public static BreakItemGoalBuilder<Void> withComponent(GoalCategory category, DataComponentType<?>... components) {
        List<DataComponentType<?>> finalComponents = new ArrayList<>(Arrays.stream(components).toList());
        finalComponents.add(DataComponents.MAX_DAMAGE);
        return new BreakItemGoalBuilder<>(
                category,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> ItemWithComponentAcceptanceCondition.hasComponents(finalComponents.toArray(DataComponentType[]::new)))
        );
    }

    @Override
    public void reifiedUpdater(ItemUtil.BrokenItem update) {}
}

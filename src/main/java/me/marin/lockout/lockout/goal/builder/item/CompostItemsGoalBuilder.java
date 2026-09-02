package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public class CompostItemsGoalBuilder<T> extends GoalBuilder<ItemUtil.CompostedItem, T> {
    public CompostItemsGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier) {
        super("COMPOST", "Compost", GoalCategory.WORKSTATIONS, optionSupplier, progressSupplier.map(ItemUtil.CompostedItem::itemStack));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                ItemTextureExtractor.item(Items.COMPOSTER),
                textureExtractor,
        10);
    }

    @Override
    public void reifiedUpdater(ItemUtil.CompostedItem update) {}

    public static CompostItemsGoalBuilder<Integer> uniqueWithComponent(int min, int max, DataComponentType<?>... components) {
        return new CompostItemsGoalBuilder<>(
                GoalOptionSupplier.integer("Items to compose", min, max, 1),
                GoalProgressSupplier.unique("Items composted", _ -> ItemWithComponentAcceptanceCondition.hasComponents(ComposterBlock.COMPOSTABLES.keySet().stream().map(ItemLike::asItem).toList(), components), ItemStack::getItem)
        );
    }
}

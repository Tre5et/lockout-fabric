package me.marin.lockout.lockout.goal.builder.block;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Collectors;

public class UseItemOnBlockGoalBuilder<T> extends GoalBuilder<BlockUtil.UsedItemOnBlock, T> {
    public UseItemOnBlockGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, BlockUtil.UsedItemOnBlock, ?> progressSupplier) {
        super("USE", "Use", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(BlockUtil.UsedItemOnBlock update) {}

    public static UseItemOnBlockGoalBuilder<Void> anyBlock(Item item, Block... blocks) {
        return new UseItemOnBlockGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    private final InListAcceptanceCondition<BlockState, Block> condition = InListAcceptanceCondition.block(blocks);

                    @Override
                    public boolean test(BlockUtil.UsedItemOnBlock value) {
                        return value.item().getItem() == item && condition.test(value.block());
                    }

                    @Override
                    public String getId() {
                        return ItemUtil.getItemId(item) + "_ON_" + condition.getId();
                    }

                    @Override
                    public String getName() {
                        return ItemUtil.getItemName(item) + " on " + condition.getId();
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples().stream().map(c -> new CornerIconTextureExtractor(
                                c,
                                ItemTextureExtractor.item(item),
                                10)
                        ).collect(Collectors.toUnmodifiableList());
                    }
                })
        );
    }

    public static UseItemOnBlockGoalBuilder<Void> anyItem(Block block, Item... items) {
        return new UseItemOnBlockGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    private final InListAcceptanceCondition<ItemStack, Item> condition = InListAcceptanceCondition.item(items);

                    @Override
                    public boolean test(BlockUtil.UsedItemOnBlock value) {
                        return value.block().getBlock().equals(block) && condition.test(value.item());
                    }

                    @Override
                    public String getId() {
                        return condition.getId() + "_ON_" + ItemUtil.getItemId(block.asItem());
                    }

                    @Override
                    public String getName() {
                        return condition.getName() + " on " + ItemUtil.getItemName(block.asItem());
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples().stream().map(c -> new CornerIconTextureExtractor(
                                ItemTextureExtractor.item(block.asItem()),
                                c,
                                10)
                        ).collect(Collectors.toUnmodifiableList());
                    }
                })
        );
    }
}

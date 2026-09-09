package me.marin.lockout.lockout.goal.builder.block;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MineBlockGoalBuilder<T> extends GoalBuilder<BlockUtil.MinedBlock, T> {
    public MineBlockGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, BlockState, ?> progressSupplier) {
        super("MINE", "Mine", GoalCategory.MINING, optionSupplier, progressSupplier.map(BlockUtil.MinedBlock::block));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                ItemTextureExtractor.item(Items.IRON_PICKAXE),
        10);
    }

    @Override
    public void reifiedUpdater(BlockUtil.MinedBlock update) {}

    public static MineBlockGoalBuilder<Void> any(Block... blocks) {
        return new MineBlockGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.block(blocks))
        );
    }

    public static MineBlockGoalBuilder<Void> unique(int count, Block... blocks) {
        return new MineBlockGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.unique("Blocks mined", _ -> InListAcceptanceCondition.block(blocks)).mapCreation(_ -> count)
        );
    }
}

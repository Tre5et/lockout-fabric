package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;

import java.util.List;

public class BlockWithItemGoalBuilder<T> extends GoalBuilder<ItemUtil.BlockedWithItem, T> {
    public BlockWithItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemUtil.BlockedWithItem, ?> progressSupplier) {
        super("BLOCK", "Block", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(ItemUtil.BlockedWithItem update) {}

    public static GoalBuilder<ItemUtil.BlockedWithItem, Void> disabled(Item item) {
        return new BlockWithItemGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(ItemUtil.BlockedWithItem value) {
                        return value.item().getItem().equals(item) && value.blockingData() != null && value.secondsToDisable() > 0;
                    }

                    @Override
                    public String getId() {
                        return "DISABLE_" + ItemUtil.getItemId(item);
                    }

                    @Override
                    public String getName() {
                        return "with " + ItemUtil.getItemName(item) + " and have it disabled";
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(ItemTextureExtractor.item(item).overlay((extractor, _, x, y, width, height, _) -> extractor.fill(x, y, x+width, y+height, ARGB.white(100)), TextureAnchor.BOTTOM_LEFT, 16, 8));
                    }
                })
        ).customName(_ -> "Have your " + ItemUtil.getItemName(item) + " disabled");
    }
}

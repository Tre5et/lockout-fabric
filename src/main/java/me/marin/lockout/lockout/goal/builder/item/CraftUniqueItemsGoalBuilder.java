package me.marin.lockout.lockout.goal.builder.item;

import lombok.NonNull;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetNumberClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class CraftUniqueItemsGoalBuilder extends GoalBuilder<ItemUtil.CraftedItem, Integer> {
    private final int min;
    private final int max;
    private final int step;

    public CraftUniqueItemsGoalBuilder(GoalCategory category, int min, int max, int step) {
        super("CRAFT_UNIQUE_" + min + "_" + max, category);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, step, (max-min+1)/step));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Items to craft", min, max, step, (max-min+1)/step));
    }

    @Override
    public @NonNull Component defaultName(Integer option) {
        return Component.literal("Craft " + option + " Unique Items");
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Integer option) {
        return ItemTextureExtractor.stack(Items.CRAFTING_TABLE, option);
    }

    @Override
    public @NonNull ServerGoalProgress<ItemUtil.CraftedItem, ?> getServerGoalProgress(Integer option) {
        return new UniqueServerGoalProgress<>(option, _ -> true);
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetNumberClientGoalProgress("Items crafted", option);
    }

    @Override
    public void reifiedUpdater(ItemUtil.CraftedItem update) {}

    public static CraftUniqueItemsGoalBuilder of(int min, int max, int step) {
        return new CraftUniqueItemsGoalBuilder(GoalCategory.MISC_ACTIONS, min, max, step);
    }
}

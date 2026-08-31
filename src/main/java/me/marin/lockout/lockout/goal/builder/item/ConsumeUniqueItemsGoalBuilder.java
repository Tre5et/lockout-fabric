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
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ConsumeUniqueItemsGoalBuilder extends GoalBuilder<ItemUtil.ConsumedItem, Integer> {
    private final int min;
    private final int max;
    private final AcceptanceCondition<ItemStack> condition;

    public ConsumeUniqueItemsGoalBuilder(String id, GoalCategory category, int min, int max, AcceptanceCondition<ItemStack> condition) {
        super("CONSUME_UNIQUE_" + id + "_" + min + "_" + max, category);
        this.min = min;
        this.max = max;
        this.condition = condition;
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, 1, max-min+1));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Items to consume", min, max, 1, max-min+1));
    }

    @Override
    public @NonNull Component defaultName(Integer option) {
        return Component.literal("Consume " + option + " of " + condition.getName());
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                new CycleTextureExtractor(condition.getExamples().stream().map(ItemTextureExtractor::new).toList()),
                new ItemCountTextureExtractor(Component.literal(option.toString()))
        ), 0);
    }

    @Override
    public @NonNull ServerGoalProgress<ItemUtil.ConsumedItem, ?> getServerGoalProgress(Integer option) {
        return new UniqueServerGoalProgress<>(option, s -> condition.test(s.itemStack()));
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetNumberClientGoalProgress("Consumed items", option);
    }

    @Override
    public void reifiedUpdater(ItemUtil.ConsumedItem update) {}

    public static ConsumeUniqueItemsGoalBuilder any(String id, int min, int max, Item... items) {
        return new ConsumeUniqueItemsGoalBuilder(id, GoalCategory.EATING_DRINKING, min, max, InListAcceptanceCondition.item(items));
    }
}

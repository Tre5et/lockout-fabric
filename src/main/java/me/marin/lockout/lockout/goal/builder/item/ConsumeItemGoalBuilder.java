package me.marin.lockout.lockout.goal.builder.item;

import lombok.NonNull;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConsumeItemGoalBuilder extends GoalBuilder<ItemUtil.ConsumedItem, Void> {
    private final List<Item> acceptableItems;
    private Predicate<ItemStack> additionalCondition = _ -> true;

    public ConsumeItemGoalBuilder(String id, GoalCategory category, List<Item> acceptableItems) {
        super("CONSUME_" + id, category);
        this.acceptableItems = acceptableItems;
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        return Component.literal(acceptableItems.stream()
                .map(ItemUtil::getItemName)
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return ItemTextureExtractor.cycleItems(acceptableItems);
    }

    @Override
    public @NonNull ServerGoalProgress<ItemUtil.ConsumedItem, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(i -> acceptableItems.contains(i.itemStack().getItem()) && additionalCondition.test(i.itemStack()));
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(ItemUtil.ConsumedItem update) {}

    public ConsumeItemGoalBuilder additionalCondition(Predicate<ItemStack> condition) {
        this.additionalCondition = condition;
        return this;
    }

    public static ConsumeItemGoalBuilder any(Item... items) {
        String id = Arrays.stream(items).map(ItemUtil::getItemId).collect(Collectors.joining("_OR_"));
        return new ConsumeItemGoalBuilder(id, GoalCategory.EATING_DRINKING, Arrays.asList(items));
    }
}

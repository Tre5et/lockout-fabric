package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetNumberClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompostUniqueItemsGoalBuilder extends GoalBuilder<ItemUtil.CompostedItem, Integer> {
    private final int min;
    private final int max;
    private final List<Item> acceptableItems;

    public CompostUniqueItemsGoalBuilder(String id, GoalCategory category, int min, int max, List<Item> acceptableItems) {
        super("COMPOST_UNIQUE_" + id, category);
        this.min = min;
        this.max = max;
        this.acceptableItems = acceptableItems;
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, 1, max-min+1));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Items to compost", min, max, 1, max-min+1));
    }

    @Override
    public Component defaultName(Integer option) {
        return Component.literal("Compost " + option + " of " + acceptableItems.stream()
                .map(ItemUtil::getItemName)
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                new CornerIconTextureExtractor(
                        ItemTextureExtractor.item(Items.COMPOSTER),
                        ItemTextureExtractor.cycleItems(acceptableItems),
                10),
                new ItemCountTextureExtractor(Component.literal(option.toString()))
        ), 0);
    }

    @Override
    public ServerGoalProgress<ItemUtil.CompostedItem, ?> getServerGoalProgress(Integer option) {
        return new UniqueServerGoalProgress<>(option, i -> acceptableItems.contains(i.item()));
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetNumberClientGoalProgress("Composted items", option);
    }

    public static CompostUniqueItemsGoalBuilder any(String id, int min, int max, Item... items) {
        return new CompostUniqueItemsGoalBuilder(id, GoalCategory.WORKSTATIONS, min, max, Arrays.stream(items).toList());
    }
}

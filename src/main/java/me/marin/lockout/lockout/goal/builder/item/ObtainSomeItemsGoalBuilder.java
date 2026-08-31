package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObtainSomeItemsGoalBuilder extends ObtainItemGoalBuilder<Void> {
    protected final int number;
    protected final List<Pair<Item, Integer>> items;

    public ObtainSomeItemsGoalBuilder(String id, int number, GoalCategory category, List<Pair<Item, Integer>> items) {
        super(id, category);
        this.number = number;
        this.items = items;
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        if(number > 1) {
            return Component.literal("Obtain " + number + " of " + items.stream()
                    .map(e -> (e.getB() > 1 ? e.getB() + " " : "") + ItemUtil.getItemName(e.getA()))
                    .collect(Collectors.joining(" or "))
            );
        } else {
            return Component.literal("Obtain " + items.stream()
                    .map(e -> (e.getB() > 1 ? e.getB() + " " : "") + ItemUtil.getItemName(e.getA()))
                    .collect(Collectors.joining(" or "))
            );
        }
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return ItemTextureExtractor.cycleStacks(items);
    }

    @Override
    boolean satisfiedBy(Inventory inventory, Void option) {
        return items.stream()
                .filter(i -> inventory.countItem(i.getA()) >= i.getB())
                .limit(number)
                .count() >= number;
    }

    public static ObtainSomeItemsGoalBuilder multiple(String id, int number, GoalCategory category, Item... items) {
        return new ObtainSomeItemsGoalBuilder(
                id,
                number,
                category,
                Arrays.stream(items).map(i -> new Pair<>(i, 1)).toList()
        );
    }

    public static ObtainSomeItemsGoalBuilder multiple(int number, GoalCategory category, Item... items) {
        return multiple(
                (number == 1 ? "" : number + "_OF_") + Arrays.stream(items)
                        .map(ObtainItemGoalBuilder::getItemId)
                        .collect(Collectors.joining("_OR_")),
                number,
                category,
                items
        );
    }

    public static ObtainSomeItemsGoalBuilder oneOf(String id, GoalCategory category, Item... items) {
        return multiple(id, 1, category, items);
    }

    public static ObtainSomeItemsGoalBuilder oneOf(GoalCategory category, Item... items) {
        return multiple(1, category, items);
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder multipleWithCounts(String id, int number, GoalCategory category, Pair<Item, Integer>... items) {
        return new ObtainSomeItemsGoalBuilder(
                id,
                number,
                category,
                Arrays.stream(items).toList()
        );
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder multipleWithCounts(int number, GoalCategory category, Pair<Item, Integer>... items) {
        return multipleWithCounts(
                (number == 1 ? "" : number + "_OF_") + Arrays.stream(items)
                        .map(i -> (i.getB() > 1 ? i.getB() + "_" : "") + getItemId(i.getA()))
                        .collect(Collectors.joining("_OR_")),
                number,
                category,
                items
        );
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder oneOfWithCounts(String id, GoalCategory category, Pair<Item, Integer>... items) {
        return multipleWithCounts(id, 1, category, items);
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder oneOfWithCounts(GoalCategory category, Pair<Item, Integer>... items) {
        return multipleWithCounts(1, category, items);
    }
}

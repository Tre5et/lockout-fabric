package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.texture.ItemTextureRenderer;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObtainSomeItemsGoalBuilder extends ObtainItemGoalBuilder<Void> {
    protected final String name;
    protected final int number;
    protected final List<Pair<Item, Integer>> items;

    public ObtainSomeItemsGoalBuilder(String id, String name, int number, GoalCategory category, List<Pair<Item, Integer>> items) {
        super(id, category);
        this.name = name;
        this.number = number;
        this.items = items;
    }

    @Override
    String getInstanceId(Void option) {
        return id;
    }

    @Override
    String getName(Void option) {
        return name;
    }

    @Override
    TextureRenderer getTextureRenderer(Void option) {
        return ItemTextureRenderer.cycleStacks(items);
    }

    @Override
    public GoalOptionGenerator<Void> optionGenerator() {
        return null;
    }

    @Override
    boolean satisfiedBy(Inventory inventory, Void option) {
        return items.stream()
                .filter(i -> inventory.countItem(i.getA()) >= i.getB())
                .limit(number)
                .count() >= number;
    }

    public static ObtainSomeItemsGoalBuilder multiple(String id, String name, int number, GoalCategory category, Item... items) {
        return new ObtainSomeItemsGoalBuilder(
                id,
                name,
                number,
                category,
                Arrays.stream(items).map(i -> new Pair<>(i, 1)).toList()
        );
    }

    public static ObtainSomeItemsGoalBuilder multiple(int number, GoalCategory category, Item... items) {
        List<String> names = Arrays.stream(items)
                .map(ObtainItemGoalBuilder::getItemName)
                .toList();
        return multiple(
                (number == 1 ? "" : number + "_OF_") + names.stream()
                        .map(String::toUpperCase)
                        .map(s -> s.replace(" ", "_"))
                        .collect(Collectors.joining("_OR_")),
                (number == 1 ? "" : number + " of ") + String.join(" or ", names),
                number,
                category,
                items
        );
    }

    public static ObtainSomeItemsGoalBuilder oneOf(String id, String name, GoalCategory category, Item... items) {
        return multiple(id, name, 1, category, items);
    }

    public static ObtainSomeItemsGoalBuilder oneOf(GoalCategory category, Item... items) {
        return multiple(1, category, items);
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder multipleWithCounts(String id, String name, int number, GoalCategory category, Pair<Item, Integer>... items) {
        return new ObtainSomeItemsGoalBuilder(
                id,
                name,
                number,
                category,
                Arrays.stream(items).toList()
        );
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder multipleWithCounts(int number, GoalCategory category, Pair<Item, Integer>... items) {
        List<Map.Entry<String, Integer>> names = Arrays.stream(items)
                .map(i -> Map.entry(
                        ObtainItemGoalBuilder.getItemName(i.getA()),
                        i.getB()
                    )
                )
                .toList();
        return multipleWithCounts(
                (number == 1 ? "" : number + "_OF_") + names.stream()
                        .map(i -> i.getValue() + "_" + i.getKey().toUpperCase().replace(" ", "_"))
                        .collect(Collectors.joining("_OR_")),
                (number == 1 ? "" : number + " of ") + names.stream()
                        .map(i -> i.getValue() + " " + i.getKey())
                        .collect(Collectors.joining(" or ")),
                number,
                category,
                items
        );
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder oneOfWithCounts(String id, String name, GoalCategory category, Pair<Item, Integer>... items) {
        return multipleWithCounts(id, name, 1, category, items);
    }

    @SafeVarargs
    public static ObtainSomeItemsGoalBuilder oneOfWithCounts(GoalCategory category, Pair<Item, Integer>... items) {
        return multipleWithCounts(1, category, items);
    }
}

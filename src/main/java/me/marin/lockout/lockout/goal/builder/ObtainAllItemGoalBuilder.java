package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.texture.ItemTextureRenderer;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ObtainAllItemGoalBuilder extends ObtainItemGoalBuilder<Void> {
    protected final String name;
    protected final List<Pair<Item, Integer>> items;

    protected ObtainAllItemGoalBuilder(String id, String name, GoalCategory category, List<Pair<Item, Integer>> items) {
        super(id, category);
        this.name = name;
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
        return items.stream().allMatch(i -> inventory.countItem(i.getA()) >= i.getB());
    }

    public static ObtainAllItemGoalBuilder simple(String id, String name, GoalCategory category, Item... items) {
        return new ObtainAllItemGoalBuilder(id,
                name,
                category,
                Arrays.stream(items).map(i -> new Pair<>(i, 1)).toList()
        );
    }

    public static ObtainAllItemGoalBuilder simple(GoalCategory category, Item... items) {
        List<String> names = Arrays.stream(items)
                .map(ObtainItemGoalBuilder::getItemName)
                .toList();
        return simple(
                names.stream()
                        .map(String::toUpperCase)
                        .map(s -> s.replace(" ", "_"))
                        .collect(Collectors.joining("_AND_")),
                String.join(" and ", names),
                category,
                items
        );
    }

    @SafeVarargs
    public static ObtainAllItemGoalBuilder withCounts(String id, String name, GoalCategory category, Pair<Item, Integer>... items) {
        return new ObtainAllItemGoalBuilder(
                id,
                name,
                category,
                Arrays.stream(items).toList()
        );
    }

    @SafeVarargs
    public static ObtainAllItemGoalBuilder withCounts(GoalCategory category, Pair<Item, Integer>... items) {
        List<Map.Entry<String, Integer>> names = Arrays.stream(items)
                .map(i -> Map.entry(
                        ObtainItemGoalBuilder.getItemName(i.getA()),
                        i.getB()
                    )
                )
                .toList();
        return withCounts(
                names.stream()
                        .map(i -> i.getValue() + "_" + i.getKey().toUpperCase().replace(" ", "_"))
                        .collect(Collectors.joining("_AND_")),
                names.stream()
                        .map(i -> i.getValue() + " " + i.getKey())
                        .collect(Collectors.joining(" and ")),
                category,
                items
        );
    }
}

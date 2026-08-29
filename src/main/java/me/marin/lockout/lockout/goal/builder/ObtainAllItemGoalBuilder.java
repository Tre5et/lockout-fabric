package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ObtainAllItemGoalBuilder extends ObtainItemGoalBuilder<Void> {
    protected final List<Pair<Item, Integer>> items;

    protected ObtainAllItemGoalBuilder(String id, GoalCategory category, List<Pair<Item, Integer>> items) {
        super(id, category);
        this.items = items;
    }

    @Override
    public NameExtractor defaultNameExtractor(Void option) {
        return NameExtractor.simple(() -> "Obtain " + items.stream()
                .map(e -> (e.getB() > 1 ? e.getB() + " " : "") + ObtainItemGoalBuilder.getItemName(e.getA()))
                .collect(Collectors.joining(" and "))
        );
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return ItemTextureExtractor.cycleStacks(items);
    }

    @Override
    boolean satisfiedBy(Inventory inventory, Void option) {
        return items.stream().allMatch(i -> inventory.countItem(i.getA()) >= i.getB());
    }

    public static ObtainAllItemGoalBuilder simple(String id, GoalCategory category, Item... items) {
        return new ObtainAllItemGoalBuilder(
                id,
                category,
                Arrays.stream(items).map(i -> new Pair<>(i, 1)).toList()
        );
    }

    public static ObtainAllItemGoalBuilder simple(GoalCategory category, Item... items) {
        return simple(
                Arrays.stream(items)
                        .map(ObtainItemGoalBuilder::getItemId)
                        .collect(Collectors.joining("_AND_")),
                category,
                items
        );
    }

    @SafeVarargs
    public static ObtainAllItemGoalBuilder withCounts(String id, GoalCategory category, Pair<Item, Integer>... items) {
        return new ObtainAllItemGoalBuilder(
                id,
                category,
                Arrays.stream(items).toList()
        );
    }

    @SafeVarargs
    public static ObtainAllItemGoalBuilder withCounts(GoalCategory category, Pair<Item, Integer>... items) {
        return withCounts(
                Arrays.stream(items)
                        .map(i -> (i.getB() > 1 ? i.getB() + "_" : "") + getItemId(i.getA()))
                        .collect(Collectors.joining("_AND_")),
                category,
                items
        );
    }
}

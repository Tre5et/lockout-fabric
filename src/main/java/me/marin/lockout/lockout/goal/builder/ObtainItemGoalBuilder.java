package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.ObtainItemGoal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.Optional;

public abstract class ObtainItemGoalBuilder<T> extends GoalBuilder<T> {
    public ObtainItemGoalBuilder(String id, GoalCategory category) {
        super("OBTAIN_" + id, category);
    }

    abstract boolean satisfiedBy(Inventory inventory, T option);

    @Override
    public Goal<?> build(T option) {
        return new ObtainItemGoal(
                getId(option),
                getNameExtractor(option),
                getTextureExtractor(option),
                getHints(null),
                new Pair<>(getStaticId(), serializeOption(option)),
                i -> satisfiedBy(i, option)
        );
    }

    protected static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }

    protected static String getItemId(Item item) {
        return Arrays.stream(BuiltInRegistries.ITEM.getKey(item).getPath().split("/")).toList().getLast().toUpperCase();
    }
}

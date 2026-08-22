package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.ObtainItemGoal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Optional;

public abstract class ObtainItemGoalBuilder<T> extends GoalBuilder<T> {
    public ObtainItemGoalBuilder(String id, GoalCategory category) {
        super("OBTAIN_" + id, category);
    }

    abstract String getInstanceId(T option);

    abstract String getName(T option);

    abstract TextureRenderer getTextureRenderer(T option);

    abstract boolean satisfiedBy(Inventory inventory, T option);

    @Override
    public Goal build(T option) {
        return new ObtainItemGoal(
                getInstanceId(option),
                "Obtain " + getName(option),
                tooltipInfo,
                getTextureRenderer(option),
                new Pair<>(getId(), serializeOption(option)),
                i -> satisfiedBy(i, option)
        );
    }

    protected static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }
}

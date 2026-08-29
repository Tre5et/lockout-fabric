package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Optional;

public abstract class ObtainItemGoalBuilder<T> extends GoalBuilder<Inventory,T> {
    public ObtainItemGoalBuilder(String id, GoalCategory category) {
        super("OBTAIN_" + id, category);
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(T option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public ServerGoalProgress<Inventory, ?> getServerGoalProgress(T option) {
        return new SimpleServerGoalProgress<>(u -> satisfiedBy(u, option));
    }

    abstract boolean satisfiedBy(Inventory inventory, T option);

    protected static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }

    protected static String getItemId(Item item) {
        return Arrays.stream(BuiltInRegistries.ITEM.getKey(item).getPath().split("/")).toList().getLast().toUpperCase();
    }
}

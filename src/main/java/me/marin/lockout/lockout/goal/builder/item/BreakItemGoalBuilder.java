package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.*;

public class BreakItemGoalBuilder extends GoalBuilder<BreakItemGoalBuilder.BrokenItem, Void> {
    private final DataComponentType<?> component;

    public BreakItemGoalBuilder(String id, GoalCategory category, DataComponentType<?> component) {
        super("BREAK_" + id, category);
        this.component = component;
    }

    @Override
    public Component defaultName(Void option) {
        return Component.literal("Break any " + BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component).getPath().replace("/", " ").replace("_", " ") + " item.");
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        List<Item> acceptableItems = new ArrayList<>(getItemsWithComponents(component, DataComponents.DAMAGE));
        Collections.shuffle(acceptableItems);
        return new StackingTextureExtractor(List.of(
                ItemTextureExtractor.cycleItems(acceptableItems),
                (e,f,x,y,w,h,t) -> e.fill(x+1, y+h-2, x+w-1, y+h-1, 0xFF000000),
                (e,f,x,y,w,h,t) -> e.fill(x+1, y+h-2, x+4, y+h-1, 0xFFFF0000)
        ), 0);
    }

    @Override
    public ServerGoalProgress<BrokenItem, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(i -> i.item().getDefaultInstance().has(component));
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(BrokenItem update) {}

    public static List<Item> getItemsWithComponents(DataComponentType<?>... components) {
        return BuiltInRegistries.ITEM.stream()
                .filter(i -> Arrays.stream(components).allMatch(c -> i.getDefaultInstance().has(c)))
                .toList();
    }

    public static BreakItemGoalBuilder withComponent(String id, GoalCategory category, DataComponentType<?> component) {
        return new BreakItemGoalBuilder(id, category, component);
    }

    public record BrokenItem(
            Item item
    ) { }
}

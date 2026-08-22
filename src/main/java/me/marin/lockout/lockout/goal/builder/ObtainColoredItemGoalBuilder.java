package me.marin.lockout.lockout.goal.builder;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.texture.ItemTextureRenderer;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

public class ObtainColoredItemGoalBuilder extends ObtainItemGoalBuilder<DyeColor> {
    protected final ColorCollection<String> names;
    protected final ColorCollection<Item> items;
    protected final int count;

    protected ObtainColoredItemGoalBuilder(String id, ColorCollection<String> names, GoalCategory category, ColorCollection<Item> items, int count) {
        super("ALL_COLORED_" + id, category);
        this.names = names;
        this.items = items;
        this.count = count;
    }

    @Override
    public GoalOptionGenerator<DyeColor> optionGenerator() {
        return GoalOptionGenerator.list(DyeColor.VALUES, new TypeToken<>() {});
    }

    @Override
    String getInstanceId(DyeColor option) {
        return id + "_" + option.getName().toUpperCase();
    }

    @Override
    String getName(DyeColor option) {
        return names.pick(option);
    }

    @Override
    TextureRenderer getTextureRenderer(DyeColor option) {
        return ItemTextureRenderer.stack(items.pick(option), count);
    }

    @Override
    boolean satisfiedBy(Inventory inventory, DyeColor option) {
        return inventory.countItem(items.pick(option)) >= count;
    }

    public static ObtainColoredItemGoalBuilder withCount(String id, ColorCollection<String> names, GoalCategory category, ColorCollection<Item> items, int count) {
        return new ObtainColoredItemGoalBuilder(id, names, category, items, count);
    }

    public static ObtainColoredItemGoalBuilder withCount(String id, GoalCategory category, ColorCollection<Item> items, int count) {
        ColorCollection<String> names = items.map(i -> (count > 1 ? count + " " : "") + getItemName(i));
        return withCount(id, names, category, items, count);
    }

    public static ObtainColoredItemGoalBuilder simple(String id, ColorCollection<String> names, GoalCategory category, ColorCollection<Item> items) {
        return withCount(id, names, category, items, 1);
    }

    public static ObtainColoredItemGoalBuilder simple(String id, GoalCategory category, ColorCollection<Item> items) {
        return withCount(id, category, items, 1);
    }
}

package me.marin.lockout.lockout.goal.builder.item;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.ListClientGoalOptionGenerator;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.ListGoalOptionGenerator;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;

import java.util.Optional;

public class ObtainColoredItemGoalBuilder extends ObtainItemGoalBuilder<DyeColor> {
    protected final ColorCollection<Item> items;
    protected final int count;

    protected ObtainColoredItemGoalBuilder(String id, GoalCategory category, ColorCollection<Item> items, int count) {
        super("COLORED_" + id, category);
        this.items = items;
        this.count = count;
        require(GoalRequirements.COLORS);
    }

    @Override
    public Optional<GoalOptionGenerator<DyeColor>> getOptionGenerator() {
        return Optional.of(new ListGoalOptionGenerator<>(DyeColor.VALUES, new TypeToken<>() {}));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<DyeColor>> getClientOptionGenerator() {
        return Optional.of(new ListClientGoalOptionGenerator<>("Color", DyeColor.VALUES, new TypeToken<>() {}));
    }

    @Override
    public String defaultId(DyeColor option) {
        return id + "_" + option.getName().toUpperCase();
    }

    @Override
    public Component defaultName(DyeColor option) {
        return Component.literal("Obtain " + (count > 1 ? count + " " : "")  + ItemUtil.getItemName(items.pick(option)));
    }

    @Override
    public TextureExtractor defaultTextureExtractor(DyeColor option) {
        return ItemTextureExtractor.stack(items.pick(option), count);
    }

    @Override
    boolean satisfiedBy(Inventory inventory, DyeColor option) {
        return inventory.countItem(items.pick(option)) >= count;
    }

    public static ObtainColoredItemGoalBuilder withCount(String id, GoalCategory category, ColorCollection<Item> items, int count) {
        return new ObtainColoredItemGoalBuilder(id, category, items, count);
    }

    public static ObtainColoredItemGoalBuilder simple(String id, GoalCategory category, ColorCollection<Item> items) {
        return withCount(id, category, items, 1);
    }
}

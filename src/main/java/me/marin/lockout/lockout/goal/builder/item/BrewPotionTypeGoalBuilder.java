package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class BrewPotionTypeGoalBuilder extends GoalBuilder<ItemUtil.BrewedItem, Void> {
    private final Item item;

    public BrewPotionTypeGoalBuilder(String id, GoalCategory category, Item item) {
        super("BREW_POTION_TYPE_" + id, category);
        this.item = item;
    }

    @Override
    public Component defaultName(Void option) {
        return Component.literal("Brew a " + BuilderUtil.identifierToName(BuiltInRegistries.ITEM.getKey(item)));
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return ItemTextureExtractor.item(item);
    }

    @Override
    public ServerGoalProgress<ItemUtil.BrewedItem, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(i -> item.equals(i.item()));
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    public static BrewPotionTypeGoalBuilder of(Item item) {
        BrewPotionTypeGoalBuilder builder = new BrewPotionTypeGoalBuilder(ItemUtil.getItemId(item), GoalCategory.BREWING, item);
        builder.group(GoalGroups.BREW_POTION);
        return builder;
    }
}

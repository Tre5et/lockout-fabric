package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.List;

public class UseItemOnEntityGoalBuilder<T> extends GoalBuilder<EntityUtil.UsedItemOnEntity, T> {
    public UseItemOnEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityUtil.UsedItemOnEntity, ?> progressSupplier) {
        super("USE", "Use", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(EntityUtil.UsedItemOnEntity update) {}

    public static GoalBuilder<EntityUtil.UsedItemOnEntity, Void> nameTag(String name, EntityType<?>... entities) {
        return new UseItemOnEntityGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities);

                    @Override
                    public boolean test(EntityUtil.UsedItemOnEntity value) {
                        return value.item().getItem().equals(Items.NAME_TAG) && value.item().has(DataComponents.CUSTOM_NAME)
                                && value.item().get(DataComponents.CUSTOM_NAME).getString().equals(name)
                                && condition.test(value.entity());
                    }

                    @Override
                    public String getId() {
                        return "NAMETAG_" + name + "_ON_" + condition.getId();
                    }

                    @Override
                    public String getName() {
                        return "Nametag with name '" + name + "' on " + condition.getName();
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples().stream()
                                .map(c -> c.overlay(ItemTextureExtractor.item(Items.NAME_TAG), TextureAnchor.TOP_RIGHT, 10))
                                .toList();
                    }
                })
        ).customName(_ -> "Name " + InListAcceptanceCondition.entity(entities).getName() + " '" + name + "'");
    }
}

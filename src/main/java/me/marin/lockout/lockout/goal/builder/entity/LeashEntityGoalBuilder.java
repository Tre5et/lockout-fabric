package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

public class LeashEntityGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick, T> {
    public LeashEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, List<Leashable>, ?> progressSupplier) {
        super("LEASH", "Leash", GoalCategory.LEASHING, optionSupplier, GoalProgressSupplier.player(progressSupplier.map(Leashable::leashableLeashedTo)));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return textureExtractor.overlay(ItemTextureExtractor.item(Items.LEAD), TextureAnchor.TOP_RIGHT, 10);
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    public static LeashEntityGoalBuilder<Void> any(EntityType<?>... entities) {
        return new LeashEntityGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.countMatching(_ -> List.of(InListAcceptanceCondition.entity(entities))).creationValue(1)
                        .map(l -> l.stream().<EntityType<?>>map(i -> {
                            if(i instanceof Entity entity) return entity.getType();
                            return null;
                        }).filter(Objects::nonNull).toList())
        );
    }

    public static LeashEntityGoalBuilder<Integer> unique(int min, int max, int step) {
        return new LeashEntityGoalBuilder<>(
                GoalOptionSupplier.integer("Entities to leash", min, max, step),
                GoalProgressSupplier.distinct(_ -> List.of(new AnyAcceptanceCondition<>(
                            "UNIQUE", () -> "Mobs at once", () -> EntityUtil.LEASHABLE.stream().map(EntityUtil::getEntityTextureExtractor).toList()
                        )
                ), l -> {
                    if(l instanceof Entity e) return e.getType();
                    return null;
                })
        );
    }
}

package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

public class AgeLockMobGoalBuilder<T> extends GoalBuilder<EntityUtil.AgeLockedEntity, T> {
    public AgeLockMobGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("AGE_LOCK", "Age Lock", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier.map(EntityUtil.AgeLockedEntity::entity));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return textureExtractor.overlay(ItemTextureExtractor.item(Items.GOLDEN_DANDELION), TextureAnchor.TOP_RIGHT, 10);
    }

    @Override
    public void reifiedUpdater(EntityUtil.AgeLockedEntity update) {}

    public static AgeLockMobGoalBuilder<Void> any() {
        return new AgeLockMobGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AnyAcceptanceCondition<>(
                        "ANY_BABY",
                        () -> "any Baby using Golden Dandelion",
                        () -> EntityUtil.AGE_LOCKABLE.stream().map(EntityUtil::getBabyEntityTextureExtractor).toList()
                ))
        );
    }
}

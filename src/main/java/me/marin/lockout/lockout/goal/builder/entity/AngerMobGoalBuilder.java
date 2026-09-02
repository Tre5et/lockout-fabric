package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class AngerMobGoalBuilder<T> extends GoalBuilder<EntityUtil.AngeredEntity, T> {
    public AngerMobGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("ANGER_", "Anger", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier.map(EntityUtil.AngeredEntity::entity));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/raid_omen.png")),
        10);
    }

    @Override
    public void reifiedUpdater(EntityUtil.AngeredEntity update) {}

    public static AngerMobGoalBuilder<Void> any(EntityType<?>... entities) {
        return new AngerMobGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities))
        );
    }
}

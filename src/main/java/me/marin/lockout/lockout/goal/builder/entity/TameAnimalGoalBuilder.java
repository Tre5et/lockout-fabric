package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.Constants;
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

public class TameAnimalGoalBuilder<T> extends GoalBuilder<EntityUtil.TamedEntity, T> {
    public TameAnimalGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("TAME", "Tame", GoalCategory.TAMING, optionSupplier, progressSupplier.map(EntityUtil.TamedEntity::entity));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/tame.png")),
        8);
    }

    @Override
    public void reifiedUpdater(EntityUtil.TamedEntity update) {}

    public static TameAnimalGoalBuilder<Void> any(EntityType<?>... entities) {
        return new TameAnimalGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities))
        );
    }
}

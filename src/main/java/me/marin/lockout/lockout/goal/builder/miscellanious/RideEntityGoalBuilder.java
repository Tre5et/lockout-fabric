package me.marin.lockout.lockout.goal.builder.miscellanious;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

public class RideEntityGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick, T> {
    public RideEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("RIDE", "Ride", GoalCategory.RIDING, optionSupplier, GoalProgressSupplier.player(progressSupplier.map(p -> p.getControlledVehicle() == null ? null : p.getControlledVehicle().getType())));
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return textureExtractor.overlay(ItemTextureExtractor.item(Items.SADDLE), TextureAnchor.TOP_RIGHT, 10);
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    public static RideEntityGoalBuilder<Void> any(EntityType<?>... entities) {
        return new RideEntityGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities))
        );
    }
}

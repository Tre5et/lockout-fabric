package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.world.entity.EntityType;

public class SpawnEntityGoalBuilder<T> extends GoalBuilder<EntityUtil.PlayerSpawnedEntity, T> {
    public SpawnEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("SPAWN", "Spawn", GoalCategory.SPAWNING, optionSupplier, progressSupplier.map(EntityUtil.PlayerSpawnedEntity::entity));
    }

    public static SpawnEntityGoalBuilder<Void> any(EntityType<?>... entities) {
        return new SpawnEntityGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.spawnEgg(entities))
        );
    }

    @Override
    public void reifiedUpdater(EntityUtil.PlayerSpawnedEntity update) {}
}

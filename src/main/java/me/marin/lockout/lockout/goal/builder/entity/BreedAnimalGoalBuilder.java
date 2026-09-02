package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class BreedAnimalGoalBuilder<T> extends GoalBuilder<EntityUtil.BredEntity, T> {

    public BreedAnimalGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier) {
        super("BREED", "Breed", GoalCategory.BREEDING, optionSupplier, progressSupplier.map(EntityUtil.BredEntity::entity));
    }

    @Override
    public void reifiedUpdater(EntityUtil.BredEntity update) {}

    public static BreedAnimalGoalBuilder<Void> any(EntityType<?>... entities) {
        return new BreedAnimalGoalBuilder<>(GoalOptionSupplier.NONE, GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities)));
    }

    public static BreedAnimalGoalBuilder<Integer> unique(int min, int max, EntityType<?>... entities) {
        return new BreedAnimalGoalBuilder<>(GoalOptionSupplier.integer("Animals to breed", min, max, 1), GoalProgressSupplier.unique("Animals bred", _ -> InListAcceptanceCondition.entity(entities)));
    }

    public static BreedAnimalGoalBuilder<Integer> unique(int min, int max) {
        return new BreedAnimalGoalBuilder<>(GoalOptionSupplier.integer("Animals to breed", min, max, 1), GoalProgressSupplier.unique("Animals bred", _ -> new AnyAcceptanceCondition<>("ANIMALS", () -> "Animals", () -> List.of(GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/full.png"))))));
    }
}

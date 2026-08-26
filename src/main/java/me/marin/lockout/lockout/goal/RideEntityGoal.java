package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import net.minecraft.world.entity.EntityType;

import java.util.function.Predicate;

public class RideEntityGoal extends SatisfiableGoal<EntityType<?>> {
    public RideEntityGoal(GoalBuildParameters parameters, Predicate<EntityType<?>> satisfiedPredicate) {
        super(parameters, satisfiedPredicate);
    }
}

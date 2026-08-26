package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.builder.GoalBuildParameters;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Predicate;

public class ObtainItemGoal extends SatisfiableGoal<Inventory> {
    public ObtainItemGoal(GoalBuildParameters parameters, Predicate<Inventory> satisfiedPredicate) {
        super(parameters, satisfiedPredicate);
    }
}

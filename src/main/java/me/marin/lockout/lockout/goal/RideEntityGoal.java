package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.texture.TextureRenderer;
import net.minecraft.world.entity.EntityType;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Predicate;

public class RideEntityGoal extends SatisfiableGoal<EntityType<?>> {
    public RideEntityGoal(String id, String name, TooltipInfo tooltipInfo, TextureRenderer textureRenderer, List<GoalHint> hints, Pair<String, String> buildData, Predicate<EntityType<?>> satisfiedPredicate) {
        super(id, name, tooltipInfo, textureRenderer, hints, buildData, satisfiedPredicate);
    }
}

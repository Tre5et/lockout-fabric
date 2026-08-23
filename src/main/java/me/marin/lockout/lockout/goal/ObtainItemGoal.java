package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.world.entity.player.Inventory;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Predicate;

public class ObtainItemGoal extends SatisfiableGoal<Inventory> {
    public ObtainItemGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, TooltipInfo tooltipInfo, List<GoalHint> hints, Pair<String, String> buildData, Predicate<Inventory> satisfiedPredicate) {
        super(id, nameExtractor, textureExtractor, tooltipInfo, hints, buildData, satisfiedPredicate);
    }
}

package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import net.minecraft.advancements.AdvancementHolder;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Predicate;

public class AdvancementGoal extends SatisfiableGoal<AdvancementHolder> {
    public AdvancementGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, TooltipInfo tooltipInfo, List<GoalHint> hints, Pair<String, String> buildData, Predicate<AdvancementHolder> satisfiedPredicate) {
        super(id, nameExtractor, textureExtractor, tooltipInfo, hints, buildData, satisfiedPredicate);
    }
}

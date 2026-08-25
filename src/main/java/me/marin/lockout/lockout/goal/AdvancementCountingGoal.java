package me.marin.lockout.lockout.goal;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.advancements.AdvancementHolder;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Function;

public class AdvancementCountingGoal extends CountingProgressGoal<AdvancementHolder> {
    public AdvancementCountingGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, List<GoalHint> hints, Pair<String, String> buildData, String title, int target, Function<AdvancementHolder, Integer> countUpStep) {
        super(id, nameExtractor, textureExtractor, hints, buildData, title, target,
                a -> a.id().getPath().startsWith("recipe") ? null : countUpStep.apply(a)
        );
    }
}

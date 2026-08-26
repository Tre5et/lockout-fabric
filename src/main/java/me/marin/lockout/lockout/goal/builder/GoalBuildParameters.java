package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import oshi.util.tuples.Pair;

import java.util.List;

public record GoalBuildParameters(
    String id,
    NameExtractor nameExtractor,
    TextureExtractor textureExtractor,
    List<GoalHint> hints,
    Pair<String, String> buildData
) { }

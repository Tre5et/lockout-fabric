package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.AdvancementCountingGoal;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;
import oshi.util.tuples.Pair;

import java.util.List;

public class AdvancementCountingGoalBuilder extends GoalBuilder<Void> {
    private final int target;

    public AdvancementCountingGoalBuilder(String id, GoalCategory category, int target) {
        super(id, category);
        this.target = target;
    }

    @Override
    public NameExtractor defaultNameExtractor(Void option) {
        return NameExtractor.simple("Obtain " + target + " unique Advancements.");
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return new StackingTextureExtractor(List.of(
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_obtained.png")),
                TextTextureExtractor.text(String.valueOf(target))
        ), 3);
    }

    @Override
    public GoalOptionGenerator<Void> optionGenerator() {
        return null;
    }

    @Override
    public Goal build(Void option) {
        return new AdvancementCountingGoal(
                getId(option),
                getNameExtractor(option),
                getTextureExtractor(option),
                getHints(option),
                new Pair<>(id, null),
                "Advancements Obtained",
                target,
                (_) -> 1
        );
    }
}

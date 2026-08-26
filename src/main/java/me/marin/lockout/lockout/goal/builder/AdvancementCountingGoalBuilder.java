package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.lockout.goal.AdvancementCountingGoal;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

public class AdvancementCountingGoalBuilder extends GoalBuilder<Integer> {
    private final int min;
    private final int max;

    public AdvancementCountingGoalBuilder(String id, GoalCategory category, int min, int max) {
        super(id, category);
        this.min = min;
        this.max = max;
    }

    @Override
    public NameExtractor defaultNameExtractor(Integer option) {
        return NameExtractor.simple("Obtain " + option + " unique Advancements");
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_obtained.png")),
                TextTextureExtractor.text(String.valueOf(option))
        ), 3);
    }

    @Override
    public GoalOptionGenerator<Integer> optionGenerator() {
        return new IntegerGoalOptionGenerator("Number of Advancements:", min, max, 1, max - min + 1);
    }

    @Override
    public Goal<?> build(Integer option) {
        return new AdvancementCountingGoal(
                getBuildParameters(option),
                "Advancements Obtained",
                option,
                (_) -> 1
        );
    }
}

package me.marin.lockout.lockout.goal.builder.experience;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.network.chat.TextColor;

import java.util.List;

public class ReachExperienceLevelGoalBuilder<T> extends GoalBuilder<ExperienceUtils.ReachedExperienceLevel, T> {
    public ReachExperienceLevelGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Integer, ?> progressSupplier) {
        super("EXPERIENCE", "Reach XP Level", GoalCategory.EXPERIENCE, optionSupplier, progressSupplier.map(ExperienceUtils.ReachedExperienceLevel::level));
    }

    @Override
    public void reifiedUpdater(ExperienceUtils.ReachedExperienceLevel update) {}

    public static ReachExperienceLevelGoalBuilder<Integer> of(int min, int max, int step) {
        return new ReachExperienceLevelGoalBuilder<>(
                GoalOptionSupplier.integer("Level to reach", min, max, step),
                GoalProgressSupplier.simple(l -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(Integer value) {
                        return value >= l;
                    }

                    @Override
                    public String getId() {
                        return l == null ? "" : l.toString();
                    }

                    @Override
                    public String getName() {
                        return l == null ? "" : l.toString();
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(TextTextureExtractor.text(l.toString(), TextColor.GREEN));
                    }
                })
        );
    }
}

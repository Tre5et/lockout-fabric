package me.marin.lockout.lockout.goal.builder.experience;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public abstract class ReachExperienceLevelGoalBuilder<T> extends GoalBuilder<ExperienceUtils.ReachedExperienceLevel, T> {

    public ReachExperienceLevelGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Integer, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(ExperienceUtils.ReachedExperienceLevel::level), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(ExperienceUtils.ReachedExperienceLevel update) {}

    public static class Total extends ReachExperienceLevelGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Total> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Reach XP level", i)
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Total::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Total(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Level to reach", min, max, step),
                    GoalProgressSupplier.simple(l -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(Integer value, ServerPlayer player) {
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
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

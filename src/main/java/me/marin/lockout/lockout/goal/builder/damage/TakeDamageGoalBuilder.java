package me.marin.lockout.lockout.goal.builder.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public abstract class TakeDamageGoalBuilder<T> extends GoalBuilder<DamageUtil.TakenDamage, T> {
    public TakeDamageGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Float, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(DamageUtil.TakenDamage::damage), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(DamageUtil.TakenDamage update) {}

    public static class Total extends TakeDamageGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Total> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("TAKE_DAMAGE", "Take", i)
                .and(GoalBuilder.numericCodec(10, i))
                .apply(i, Total::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Total(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Amount of damage to take", min, max, step),
                    GoalProgressSupplier.rawTotal("Damage taken", () -> "Damage",
                            () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/half.png"))
                    ).mapCreation(i -> (float)i),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
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

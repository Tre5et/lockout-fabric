package me.marin.lockout.lockout.goal.builder.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public abstract class DealDamageGoalBuilder<T> extends GoalBuilder<DamageUtil.DealtDamage, T> {
    public DealDamageGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Float, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(DamageUtil.DealtDamage::damage), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                ItemTextureExtractor.item(Items.IRON_SWORD),
        10);
    }

    @Override
    public void reifiedUpdater(DamageUtil.DealtDamage update) {}

    public static class Total extends DealDamageGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Total> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Deal", i)
                .and(GoalBuilder.numericCodec(25, i))
                .apply(i, Total::new)
        );

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Total(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Amount of damage to deal", min, max, step),
                    GoalProgressSupplier.rawTotal("Damage dealt", () -> "Damage",
                            () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/half.png"))
                    ).mapCreation(i -> (float)i),
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

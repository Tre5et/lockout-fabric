package me.marin.lockout.lockout.goal.builder.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public abstract class AgeLockMobGoalBuilder<T> extends GoalBuilder<EntityUtil.AgeLockedEntity, T> {

    public AgeLockMobGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityUtil.AgeLockedEntity, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return textureExtractor.overlay(ItemTextureExtractor.item(Items.GOLDEN_DANDELION), TextureAnchor.TOP_RIGHT, 10);
    }

    @Override
    public void reifiedUpdater(EntityUtil.AgeLockedEntity update) {}

    public static class Any extends AgeLockMobGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("AGE_LOCK", "Age Lock", i)
                .apply(i, Any::new));

        public Any(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AnyAcceptanceCondition<>(
                            "ANY_BABY",
                            () -> "any Baby using Golden Dandelion",
                            () -> EntityUtil.AGE_LOCKABLE.stream().map(EntityUtil::getBabyEntityTextureExtractor).toList()
                    )),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

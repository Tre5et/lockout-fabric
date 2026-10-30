package me.marin.lockout.lockout.goal.builder.miscellanious;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;

import java.util.List;
import java.util.Optional;

public abstract class CustomEventGoalBuilder<T> extends GoalBuilder<CustomEvent, T> {

    public CustomEventGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, CustomEvent, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(CustomEvent update) {}

    public static class Exactly extends CustomEventGoalBuilder<Void> {
        public static final MapCodec<Exactly> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .and(RegistryCodecs.holder(LockoutRegistryKeys.CUSTOM_EVENT).fieldOf("event").forGetter(Exactly::getEvent))
                .apply(i, Exactly::new));

        @Getter
        private final Holder<CustomEvent> event;

        public Exactly(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<CustomEvent> event) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> event.value().getAcceptanceCondition()),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.event = event;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

package me.marin.lockout.lockout.goal.builder.miscellanious;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public abstract class ObtainAdvancementGoalBuilder<T> extends GoalBuilder<AdvancementHolder, T> {

    public ObtainAdvancementGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Identifier, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(AdvancementHolder::id), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(AdvancementHolder update) {}

    public static class Any extends ObtainAdvancementGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Obtain", i)
                .and(RegistryCodecs.holderSet(Registries.ADVANCEMENT).fieldOf("advancements").forGetter(Any::getAdvancements))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<Advancement> advancements;

        public Any(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Advancement> advancements) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.advancement(advancements.stream().map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).map(ResourceKey::identifier).toList())),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.advancements = advancements;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends ObtainAdvancementGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Obtain", i)
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Unique(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Advancements to obtain", min, max, step),
                    GoalProgressSupplier.unique("Advancements obtained", _ -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(Identifier value, ServerPlayer player) {
                            return !value.getPath().startsWith("recipes/") && !value.getPath().endsWith("/root");
                        }

                        @Override
                        public String getId() {
                            return "";
                        }

                        @Override
                        public String getName() {
                            return "Advancements";
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_obtained.png")));
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

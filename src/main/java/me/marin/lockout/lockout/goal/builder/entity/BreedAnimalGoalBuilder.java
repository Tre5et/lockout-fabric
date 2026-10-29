package me.marin.lockout.lockout.goal.builder.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
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
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BreedAnimalGoalBuilder<T> extends GoalBuilder<EntityUtil.BredEntity, T> {

    public BreedAnimalGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(EntityUtil.BredEntity::entity), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/full.png")),
        8);
    }

    @Override
    public void reifiedUpdater(EntityUtil.BredEntity update) {}

    public static class Any extends BreedAnimalGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("BREED", "Breed", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Any::getEntities))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Any(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE, GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList()))), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends BreedAnimalGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("BREED", "Breed", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).optionalFieldOf("entities").forGetter(Unique::getEntities))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final Optional<HolderSet<EntityType<?>>> entities;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Unique(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Optional<HolderSet<EntityType<?>>> entities, int min, int max, int step) {
            super(
                    GoalOptionSupplier.integer("Animals to breed", min, max, 1),
                    entities.isPresent() ? GoalProgressSupplier.unique("Animals bred", _ -> InListAcceptanceCondition.entity(entities.get().stream().map(Holder::value).collect(Collectors.toUnmodifiableList()))) : GoalProgressSupplier.unique("Animals bred", _ -> new AnyAcceptanceCondition<>("ANIMALS", () -> "Animals", () -> EntityUtil.BREEDABLE.stream().map(EntityUtil::getEntityTextureExtractor).toList())),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entities = entities;
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

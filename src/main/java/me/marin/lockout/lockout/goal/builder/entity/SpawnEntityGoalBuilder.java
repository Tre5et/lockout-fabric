package me.marin.lockout.lockout.goal.builder.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SpawnEntityGoalBuilder<T> extends GoalBuilder<EntityUtil.PlayerSpawnedEntity, T> {

    public SpawnEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, EntityType<?>, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(EntityUtil.PlayerSpawnedEntity::entity), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(EntityUtil.PlayerSpawnedEntity update) {}

    public static class Any extends SpawnEntityGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Spawn", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Any::getEntities))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Any(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.spawnEgg(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList()))),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

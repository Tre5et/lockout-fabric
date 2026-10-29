package me.marin.lockout.lockout.goal.builder.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DeathGoalBuilder<T> extends GoalBuilder<DamageUtil.PlayerDied, T> {
    public DeathGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, DamageUtil.PlayerDied, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/death.png")),
        7);
    }

    @Override
    public void reifiedUpdater(DamageUtil.PlayerDied update) {}

    public static class Type extends DeathGoalBuilder<Void> {
        public static final MapCodec<Type> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("DEATH", "Die to", i)
                .and(i.group(
                        RegistryCodecs.holderSet(Registries.DAMAGE_TYPE).fieldOf("types").forGetter(Type::getTypes),
                        TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(Type::getTextureExtractorProvider)
                )).apply(i, Type::new));

        @Getter
        private final HolderSet<DamageType> types;
        @Getter
        private final TextureExtractorProvider textureExtractorProvider;

        public Type(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<DamageType> types, TextureExtractorProvider textureExtractorProvider) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.damageType(textureExtractorProvider, types.stream().map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList()).mapEquals((s, t) -> s.source().typeHolder().is(t))),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.types = types;
            this.textureExtractorProvider = textureExtractorProvider;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Entity extends DeathGoalBuilder<Void> {
        public static final MapCodec<Entity> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("DEATH", "Die to", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Entity::getEntities))
                .apply(i, Entity::new));

        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Entity(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())).map(d -> d.source().getEntity() != null ? d.source().getEntity().getType() : (d.source().getDirectEntity() != null ? d.source().getDirectEntity().getType() : null))), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class FallOffBlock extends DeathGoalBuilder<Void> {
        public static final MapCodec<FallOffBlock> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("DEATH", "Die to", i)
                .and(RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(FallOffBlock::getBlocks))
                .apply(i, FallOffBlock::new));

        @Getter
        private final HolderSet<Block> blocks;

        public FallOffBlock(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Block> blocks) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.fallLocation(blocks.stream().map(b -> new Pair<FallLocation, Supplier<TextureExtractor>>(FallLocation.blockToFallLocation(b.value().defaultBlockState()), () -> ItemTextureExtractor.item(b.value().asItem()))).toList()).map(DamageUtil.PlayerDied::player)),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.blocks = blocks;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

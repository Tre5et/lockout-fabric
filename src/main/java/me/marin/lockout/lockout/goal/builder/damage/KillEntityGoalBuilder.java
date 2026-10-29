package me.marin.lockout.lockout.goal.builder.damage;

import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
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
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class KillEntityGoalBuilder<T> extends GoalBuilder<DamageUtil.KilledEntity, T> {
    public KillEntityGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, DamageUtil.KilledEntity, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                ItemTextureExtractor.item(Items.IRON_SWORD),
        10);
    }

    @Override
    public void reifiedUpdater(DamageUtil.KilledEntity update) {}

    public static class Any extends KillEntityGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Any::getEntities))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Any(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())).map(e -> e.entity().getType())),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends KillEntityGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Unique::getEntities))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;
        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Unique(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Mobs to kill", min, max, step),
                    GoalProgressSupplier.unique("Mobs killed", _ -> InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())).map(e -> e.entity().getType()), e -> e.entity().getType()),
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

    public static class Total extends KillEntityGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Total> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .and(RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Total::getEntities))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Total::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;
        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Total(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<EntityType<?>> entities, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Mobs to kill", min, max, step),
                    GoalProgressSupplier.countTotal("Mobs killed", _ -> InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())).map(e -> e.entity().getType())),
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

    public static class ColoredSheep extends KillEntityGoalBuilder<DyeColor> {
        public static final MapCodec<ColoredSheep> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .apply(i, ColoredSheep::new));

        public ColoredSheep(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "Color", DyeColor::toString),
                    GoalProgressSupplier.simple(c -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(DamageUtil.KilledEntity value, ServerPlayer player) {
                            return value.entity().getType() == EntityTypes.SHEEP && ((Sheep) value.entity()).getColor() == c;
                        }

                        @Override
                        public String getId() {
                            return "COLORED_SHEEP";
                        }

                        @Override
                        public String getName() {
                            return BuilderUtil.idToName(c.getName()) + " Sheep";
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(EntityUtil.getEntityTextureExtractor(EntityTypes.SHEEP).overlay(ItemTextureExtractor.item(Items.DYE.pick(c)), TextureAnchor.BOTTOM_RIGHT, 10));
                        }
                    }), idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);

            ArrayList<Holder<GoalRequirement>> req = new ArrayList<>(List.of(Holder.direct(GoalRequirements.COLORS)));
            req.addAll(requirements.stream().toList());
            this.requirements = HolderSet.direct(req);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class DeathType extends KillEntityGoalBuilder<Void> {
        public static final MapCodec<DeathType> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .and(i.group(
                        RegistryCodecs.holderSet(Registries.DAMAGE_TYPE).fieldOf("damage_types").forGetter(DeathType::getDamageType),
                        TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(DeathType::getTexture),
                        RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(DeathType::getEntities)
                )).apply(i, DeathType::new));

        @Getter
        private final HolderSet<DamageType> damageType;
        @Getter
        private final TextureExtractorProvider texture;
        @Getter
        private final HolderSet<EntityType<?>> entities;

        public DeathType(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<DamageType> damageTypes, TextureExtractorProvider texture, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList()));
                        private final InListAcceptanceCondition<Holder<DamageType>, ResourceKey<DamageType>> damageCondition = InListAcceptanceCondition.damageType(texture, damageTypes.stream().map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList()).mapEquals(Holder::is);

                        @Override
                        public boolean test(DamageUtil.KilledEntity value, ServerPlayer player) {
                            return condition.test(value.entity().getType(), player) && damageCondition.test(value.source().typeHolder(), player);
                        }

                        @Override
                        public String getId() {
                            return "METHOD_" + damageCondition.getId() + "_" + condition.getId();
                        }

                        @Override
                        public String getName() {
                            return condition.getName() + " using " + damageCondition.getName();
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return condition.getExamples().stream().map(e -> e.overlay(new CycleTextureExtractor(damageCondition.getExamples()), TextureAnchor.BOTTOM_RIGHT, 10)).toList();
                        }
                    }),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.damageType = damageTypes;
            this.texture = texture;
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Dimension extends KillEntityGoalBuilder<Void> {
        public static final MapCodec<Dimension> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("KILL", "Kill", i)
                .and(i.group(
                        RegistryCodecs.holderSet(Registries.DIMENSION).fieldOf("dimension").forGetter(Dimension::getDimensions),
                        TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(Dimension::getTexture),
                        RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Dimension::getEntities)
                )).apply(i, Dimension::new));

        @Getter
        private final HolderSet<Level> dimensions;
        @Getter
        private final TextureExtractorProvider texture;
        @Getter
        private final HolderSet<EntityType<?>> entities;

        public Dimension(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Level> dimensions, TextureExtractorProvider texture, HolderSet<EntityType<?>> entities) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList()));

                        @Override
                        public boolean test(DamageUtil.KilledEntity value, ServerPlayer player) {
                            return condition.test(value.entity().getType(), player) && dimensions.stream().anyMatch(d -> d.value().dimension().equals(value.entity().level().dimension()));
                        }

                        @Override
                        public String getId() {
                            return "DIMENSION_" + dimensions.stream().map(d -> BuilderUtil.identifierToId(d.value().dimension().identifier())).collect(Collectors.joining("_OR_")) + "_" + condition.getId();
                        }

                        @Override
                        public String getName() {
                            return condition.getName() + " in " + dimensions.stream().map(d -> BuilderUtil.identifierToName(d.value().dimension().identifier())).collect(Collectors.joining(" or "));
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return condition.getExamples().stream().map(e -> new StackingTextureExtractor(
                                    List.of(e, texture.get(null)), 6
                            )).collect(Collectors.toUnmodifiableList());
                        }
                    }),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.dimensions = dimensions;
            this.texture = texture;
            this.entities = entities;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

package me.marin.lockout.lockout.goal.builder.statistic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
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
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public abstract class ChangeStatisticGoalBuilder<T> extends GoalBuilder<StatisticUtil.StatisticChanged, T> {

    public ChangeStatisticGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, StatisticUtil.StatisticChanged, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(StatisticUtil.StatisticChanged update) {}

    public static class Custom extends ChangeStatisticGoalBuilder<Void> {
        public static final MapCodec<Custom> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("STATISTIC", "", i)
                .and(i.group(
                        TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(Custom::getTexture),
                        RegistryCodecs.holderSet(Registries.CUSTOM_STAT).fieldOf("statistics").forGetter(Custom::getStatistics)
                )).apply(i, Custom::new));

        @Getter
        private final TextureExtractorProvider texture;
        @Getter
        private final HolderSet<Identifier> statistics;

        public Custom(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, TextureExtractorProvider texture, HolderSet<Identifier> statistics) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(texture, statistics.stream().map(Holder::value).map(Stats.CUSTOM::get).collect(Collectors.toUnmodifiableList()))).map(StatisticUtil.StatisticChanged::statistic),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.texture = texture;
            this.statistics = statistics;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class CustomTotal extends ChangeStatisticGoalBuilder<Integer> implements NumericRange {
        private record BaseFields(String idPrefix, String namePrefix, Holder<GoalCategory> category,
                                  GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements,
                                  Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider,
                                  Optional<TextureExtractorProvider> customTextureExtractorProvider,
                                  HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            private static BaseFields from(CustomTotal builder) {
                return new BaseFields(builder.getIdPrefix(), builder.getNamePrefix(), builder.getCategory(),
                        builder.getDifficultySupplier(), builder.getRequirements(),
                        Optional.ofNullable(builder.getCustomIdProvider()), Optional.ofNullable(builder.getCustomNameProvider()),
                        Optional.ofNullable(builder.getCustomTextureExtractorProvider()), builder.getGroups(),
                        builder.getHints(), builder.isEnabled());
            }
        }

        private record Fields(TextureExtractorProvider texture, HolderSet<Identifier> statistics,
                              String creationName, String progressName, double scale, @Getter int min, @Getter int max, @Getter int step) implements NumericRange {
            private static Fields from(CustomTotal builder) {
                return new Fields(builder.texture, builder.statistics, builder.creationName, builder.progressName,
                        builder.scale, builder.min, builder.max, builder.step);
            }
        }

        private static final MapCodec<BaseFields> BASE_FIELDS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                        Codec.STRING.optionalFieldOf("id_prefix", "STATISTIC").forGetter(BaseFields::idPrefix),
                        Codec.STRING.optionalFieldOf("name_prefix", "").forGetter(BaseFields::namePrefix),
                        RegistryCodecs.holder(LockoutRegistryKeys.GOAL_CATEGORY).fieldOf("category").forGetter(BaseFields::category),
                        GoalDifficultySupplier.CODEC.fieldOf("difficulty").forGetter(BaseFields::difficultySupplier),
                        RegistryCodecs.holderSet(LockoutRegistryKeys.GOAL_REQUIREMENT).optionalFieldOf("requirements", HolderSet.empty()).forGetter(BaseFields::requirements),
                        IdProvider.CODEC.optionalFieldOf("id").forGetter(BaseFields::customIdProvider),
                        NameProvider.CODEC.optionalFieldOf("name").forGetter(BaseFields::customNameProvider),
                        TextureExtractorProvider.CODEC.optionalFieldOf("texture").forGetter(BaseFields::customTextureExtractorProvider),
                        RegistryCodecs.holderSet(LockoutRegistryKeys.GOAL_GROUP).optionalFieldOf("groups", HolderSet.empty()).forGetter(BaseFields::groups),
                        Codec.list(HintCombination.CODEC).optionalFieldOf("hints", List.of()).forGetter(BaseFields::hints),
                        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(BaseFields::enabled)
                ).apply(i, BaseFields::new));

        private static final MapCodec<Fields> FIELDS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                            TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(Fields::texture),
                            RegistryCodecs.holderSet(Registries.CUSTOM_STAT).fieldOf("statistics").forGetter(Fields::statistics),
                            Codec.STRING.fieldOf("creation_name").forGetter(Fields::creationName),
                            Codec.STRING.fieldOf("progress_name").forGetter(Fields::progressName),
                            Codec.DOUBLE.optionalFieldOf("scale", 1d).forGetter(Fields::scale)
                ).and(GoalBuilder.numericCodec(1, i))
                .apply(i, Fields::new));

        public static final MapCodec<CustomTotal> MAP_CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(BASE_FIELDS_CODEC.keys(ops), FIELDS_CODEC.keys(ops));
            }

            @Override
            public <T> DataResult<CustomTotal> decode(DynamicOps<T> ops, MapLike<T> input) {
                return BASE_FIELDS_CODEC.decode(ops, input).flatMap(base ->
                        FIELDS_CODEC.decode(ops, input).map(fields -> create(base, fields)));
            }

            @Override
            public <T> RecordBuilder<T> encode(CustomTotal value, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                RecordBuilder<T> withBase = BASE_FIELDS_CODEC.encode(BaseFields.from(value), ops, prefix);
                return FIELDS_CODEC.encode(Fields.from(value), ops, withBase);
            }
        };

        private static CustomTotal create(BaseFields base, Fields fields) {
            return new CustomTotal(base.idPrefix(), base.namePrefix(), base.category(), base.difficultySupplier(),
                    base.requirements(), base.customIdProvider(), base.customNameProvider(),
                    base.customTextureExtractorProvider(), base.groups(), base.hints(), base.enabled(),
                    fields.texture(), fields.statistics(), fields.creationName(), fields.progressName(),
                    fields.scale(), fields.min(), fields.max(), fields.step());
        }

        @Getter
        private final TextureExtractorProvider texture;
        @Getter
        private final HolderSet<Identifier> statistics;
        @Getter
        private final String creationName;
        @Getter
        private final String progressName;
        @Getter
        private final double scale;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public CustomTotal(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, TextureExtractorProvider texture, HolderSet<Identifier> statistics, String creationName, String progressName, double scale, int min, int max, int step) {
            super(GoalOptionSupplier.integer(creationName, min, max, step),
                    GoalProgressSupplier.total(progressName, _ -> InListAcceptanceCondition.statistic(texture, statistics.stream().map(Holder::value).map(Stats.CUSTOM::get).collect(Collectors.toUnmodifiableList())).map(StatisticUtil.StatisticChanged::statistic), StatisticUtil.StatisticChanged::amount, n -> String.valueOf(n.doubleValue() / scale)).mapCreation(n -> n.doubleValue() * scale),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.texture = texture;
            this.statistics = statistics;
            this.creationName = creationName;
            this.progressName = progressName;
            this.scale = scale;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class ItemInteraction extends ChangeStatisticGoalBuilder<Void> {
        public static final MapCodec<ItemInteraction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("STATISTICS", "", i)
                .and(i.group(
                        RegistryCodecs.holder(Registries.STAT_TYPE)
                                .comapFlatMap(s -> Registries.ITEM.isFor(s.value().getRegistry().key()) ? DataResult.success(Holder.direct((StatType<Item>)s.value())) : DataResult.error(() -> "StatType is not an item stat"), s -> Holder.direct(s.value()))
                                .fieldOf("statistic").forGetter(ItemInteraction::getStatistic),
                        RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(ItemInteraction::getItems)
                )).apply(i, ItemInteraction::new));

        @Getter
        private final Holder<StatType<Item>> statistic;
        @Getter
        private final HolderSet<Item> items;

        public ItemInteraction(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<StatType<Item>> statistic, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(_ -> ItemTextureExtractor.cycleItems(items.stream().map(Holder::value).toList()), items.stream().map(Holder::value).map(i -> statistic.value().get(i)).collect(Collectors.toUnmodifiableList()))).map(StatisticUtil.StatisticChanged::statistic),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.statistic = statistic;
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class ItemInteractionWhileRiding extends ChangeStatisticGoalBuilder<Void> {
        public static final MapCodec<ItemInteractionWhileRiding> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("STATISTICS", "", i)
                .and(i.group(
                        RegistryCodecs.holder(Registries.STAT_TYPE)
                                .comapFlatMap(s -> Registries.ITEM.isFor(s.value().getRegistry().key()) ? DataResult.success(Holder.direct((StatType<Item>)s.value())) : DataResult.error(() -> "StatType is not an item stat"), s -> Holder.direct(s.value()))
                                .fieldOf("statistic").forGetter(ItemInteractionWhileRiding::getStatistic),
                        RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(ItemInteractionWhileRiding::getItems),
                        RegistryCodecs.holder(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(ItemInteractionWhileRiding::getEntity)
                )).apply(i, ItemInteractionWhileRiding::new));

        @Getter
        private final Holder<StatType<Item>> statistic;
        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final Holder<EntityType<?>> entity;

        public ItemInteractionWhileRiding(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<StatType<Item>> statistic, HolderSet<Item> items, Holder<EntityType<?>> entity) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(_ -> ItemTextureExtractor.cycleItems(items.stream().map(Holder::value).toList()), items.stream().map(Holder::value).map(i -> statistic.value().get(i)).collect(Collectors.toUnmodifiableList()))
                            .withPlayerRequirement(p -> p.isPassenger() && p.getVehicle() != null && p.getVehicle().is(entity.value()))
                    ).map(StatisticUtil.StatisticChanged::statistic),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.statistic = statistic;
            this.items = items;
            this.entity = entity;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

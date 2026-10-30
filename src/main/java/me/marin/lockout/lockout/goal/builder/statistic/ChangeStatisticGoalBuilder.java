package me.marin.lockout.lockout.goal.builder.statistic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
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
import java.util.stream.Collectors;

public abstract class ChangeStatisticGoalBuilder<T> extends GoalBuilder<StatisticUtil.StatisticChanged, T> {

    public ChangeStatisticGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, StatisticUtil.StatisticChanged, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(StatisticUtil.StatisticChanged update) {}

    public static class Custom extends ChangeStatisticGoalBuilder<Void> {
        public static final MapCodec<Custom> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .and(i.group(
                        TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(Custom::getTexture),
                        RegistryCodecs.holderSet(Registries.CUSTOM_STAT).fieldOf("statistics").forGetter(Custom::getStatistics)
                )).apply(i, Custom::new));

        @Getter
        private final TextureExtractorProvider texture;
        @Getter
        private final HolderSet<Identifier> statistics;

        public Custom(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, TextureExtractorProvider texture, HolderSet<Identifier> statistics) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(texture, statistics.stream().map(Holder::value).map(Stats.CUSTOM::get).collect(Collectors.toUnmodifiableList()))).map(StatisticUtil.StatisticChanged::statistic),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.texture = texture;
            this.statistics = statistics;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class CustomTotal extends ChangeStatisticGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<CustomTotal> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .and(i.group(TextureExtractorProvider.CODEC.fieldOf("texture").forGetter(CustomTotal::getTexture),
                        RegistryCodecs.holderSet(Registries.CUSTOM_STAT).fieldOf("statistics").forGetter(CustomTotal::getStatistics),
                        Codec.STRING.fieldOf("creation_name").forGetter(CustomTotal::getCreationName),
                        Codec.STRING.fieldOf("progress_name").forGetter(CustomTotal::getProgressName),
                        Codec.DOUBLE.optionalFieldOf("scale", 1d).forGetter(CustomTotal::getScale)
                ).and(GoalBuilder.numericCodec(1, i)))
                .apply(i, CustomTotal::new));

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

        public CustomTotal(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, TextureExtractorProvider texture, HolderSet<Identifier> statistics, String creationName, String progressName, double scale, int min, int max, int step) {
            super(GoalOptionSupplier.integer(creationName, min, max, step),
                    GoalProgressSupplier.total(progressName, _ -> InListAcceptanceCondition.statistic(texture, statistics.stream().map(Holder::value).map(Stats.CUSTOM::get).collect(Collectors.toUnmodifiableList())).map(StatisticUtil.StatisticChanged::statistic), StatisticUtil.StatisticChanged::amount, n -> String.valueOf(n.doubleValue() / scale)).mapCreation(n -> n.doubleValue() * scale),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
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
        public static final MapCodec<ItemInteraction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
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

        public ItemInteraction(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<StatType<Item>> statistic, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(_ -> ItemTextureExtractor.cycleItems(items.stream().map(Holder::value).toList()), items.stream().map(Holder::value).map(i -> statistic.value().get(i)).collect(Collectors.toUnmodifiableList()))).map(StatisticUtil.StatisticChanged::statistic),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.statistic = statistic;
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class ItemInteractionWhileRiding extends ChangeStatisticGoalBuilder<Void> {
        public static final MapCodec<ItemInteractionWhileRiding> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
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

        public ItemInteractionWhileRiding(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<StatType<Item>> statistic, HolderSet<Item> items, Holder<EntityType<?>> entity) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(_ -> ItemTextureExtractor.cycleItems(items.stream().map(Holder::value).toList()), items.stream().map(Holder::value).map(i -> statistic.value().get(i)).collect(Collectors.toUnmodifiableList()))
                            .withPlayerRequirement(p -> p.isPassenger() && p.getVehicle() != null && p.getVehicle().is(entity.value()))
                    ).map(StatisticUtil.StatisticChanged::statistic),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
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

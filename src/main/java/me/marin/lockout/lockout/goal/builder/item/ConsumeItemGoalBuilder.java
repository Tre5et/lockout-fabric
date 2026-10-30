package me.marin.lockout.lockout.goal.builder.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ConsumeItemGoalBuilder<T> extends GoalBuilder<ItemUtil.ConsumedItem, T> {

    public ConsumeItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(ItemUtil.ConsumedItem::itemStack), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(ItemUtil.ConsumedItem update) {}

    public static class Any extends ConsumeItemGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Consume", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(Any::getItems))
                .apply(i, Any::new));

        @Getter
        private final HolderSet<Item> items;

        public Any(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items.stream().map(Holder::value).toList())),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends ConsumeItemGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Consume", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(Unique::getItems))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Unique(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Items to consume", min, max, 1),
                    GoalProgressSupplier.unique("Items consumed", _ -> InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()), ItemStack::getItem),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class UniqueWithComponents extends ConsumeItemGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<UniqueWithComponents> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Consume", i)
                .and(RegistryCodecs.holderSet(Registries.DATA_COMPONENT_TYPE).fieldOf("components").forGetter(UniqueWithComponents::getComponents))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, UniqueWithComponents::new));

        @Getter
        private final HolderSet<DataComponentType<?>> components;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public UniqueWithComponents(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<DataComponentType<?>> components, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Items to consume", min, max, 1),
                    GoalProgressSupplier.unique("Items consumed", _ -> ItemWithComponentAcceptanceCondition.hasComponents(components.stream().map(Holder::value).collect(Collectors.toUnmodifiableList())), ItemStack::getItem),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.components = components;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class WithStatusEffect extends ConsumeItemGoalBuilder<Void> {
        public static final MapCodec<WithStatusEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Consume", i)
                .and(RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(WithStatusEffect::getItems))
                .apply(i, WithStatusEffect::new));

        @Getter
        private final HolderSet<Item> items;

        public WithStatusEffect(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.item(items.stream().map(Holder::value).toList()).withPlayerRequirement(p -> !p.getActiveEffects().isEmpty())),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

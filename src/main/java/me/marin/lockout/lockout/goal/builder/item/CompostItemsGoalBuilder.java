package me.marin.lockout.lockout.goal.builder.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.NumericRange;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CompostItemsGoalBuilder<T> extends GoalBuilder<ItemUtil.CompostedItem, T> {

    public CompostItemsGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(ItemUtil.CompostedItem::itemStack), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                ItemTextureExtractor.item(Items.COMPOSTER),
                textureExtractor,
        10);
    }

    @Override
    public void reifiedUpdater(ItemUtil.CompostedItem update) {}

    public static class UniqueWithComponent extends CompostItemsGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<UniqueWithComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Compost", i)
                .and(RegistryCodecs.holderSet(Registries.DATA_COMPONENT_TYPE).fieldOf("components").forGetter(UniqueWithComponent::getComponents))
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, UniqueWithComponent::new));

        @Getter
        private final HolderSet<DataComponentType<?>> components;
        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public UniqueWithComponent(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<DataComponentType<?>> components, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Items to compost", min, max, 1),
                    GoalProgressSupplier.unique("Items composted", _ -> {
                        List<DataComponentType<?>> itemComponents = new ArrayList<>(components.stream().map(Holder::value).toList());
                        itemComponents.add(DataComponents.COMPOSTABLE);
                        return ItemWithComponentAcceptanceCondition.hasComponents(BuiltInRegistries.ITEM.stream().toList(), itemComponents);
                    }, ItemStack::getItem),
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
}

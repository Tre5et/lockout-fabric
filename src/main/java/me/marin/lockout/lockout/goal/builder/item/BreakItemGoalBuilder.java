package me.marin.lockout.lockout.goal.builder.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.ItemWithComponentAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Stream;

public abstract class BreakItemGoalBuilder<T> extends GoalBuilder<ItemUtil.BrokenItem, T> {

    public BreakItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(i -> i.item().getDefaultInstance()), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        ItemStack stack = Items.GOLDEN_SWORD.getDefaultInstance();
        stack.set(DataComponents.DAMAGE, 27);
        return new StackingTextureExtractor(List.of(textureExtractor, new ItemDecorationTextureExtractor(stack)), 0);
    }

    @Override
    public void reifiedUpdater(ItemUtil.BrokenItem update) {}

    public static class Any extends BreakItemGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Break", i)
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

    public static class Component extends BreakItemGoalBuilder<Void> {
        public static final MapCodec<Component> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Break", i)
                .and(RegistryCodecs.holderSet(Registries.DATA_COMPONENT_TYPE).fieldOf("components").forGetter(Component::getComponents))
                .apply(i, Component::new));

        @Getter
        private final HolderSet<DataComponentType<?>> components;

        public Component(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<DataComponentType<?>> components) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> ItemWithComponentAcceptanceCondition.hasComponents(Stream.concat(components.stream().map(Holder::value), Stream.of((DataComponentType<?>)DataComponents.MAX_DAMAGE)).toList())),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.components = components;
        }
        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

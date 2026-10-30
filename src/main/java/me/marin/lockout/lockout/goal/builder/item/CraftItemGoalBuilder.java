package me.marin.lockout.lockout.goal.builder.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public abstract class CraftItemGoalBuilder<T> extends GoalBuilder<ItemUtil.CraftedItem, T> {

    public CraftItemGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ItemStack, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(ItemUtil.CraftedItem::itemStack), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(ItemUtil.CraftedItem update) {}

    public static class Any extends CraftItemGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Craft", i)
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

    public static class Unique extends CraftItemGoalBuilder<Integer> implements NumericRange {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Craft", i)
                .and(GoalBuilder.numericCodec(1, i))
                .apply(i, Unique::new));

        @Getter
        private final int min;
        @Getter
        private final int max;
        @Getter
        private final int step;

        public Unique(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int min, int max, int step) {
            super(GoalOptionSupplier.integer("Items to craft", min, max, step),
                    GoalProgressSupplier.unique("Items crafted", _ -> new AnyAcceptanceCondition<>("ITEMS", () -> "Items", () -> List.of(ItemTextureExtractor.item(Items.CRAFTING_TABLE))), ItemStack::getItem),
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

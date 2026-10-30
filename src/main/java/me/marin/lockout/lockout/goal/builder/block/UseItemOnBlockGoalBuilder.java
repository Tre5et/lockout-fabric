package me.marin.lockout.lockout.goal.builder.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class UseItemOnBlockGoalBuilder<T> extends GoalBuilder<BlockUtil.UsedItemOnBlock, T> {
    public UseItemOnBlockGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, BlockUtil.UsedItemOnBlock, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(BlockUtil.UsedItemOnBlock update) {}

    public static class Any extends UseItemOnBlockGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("Use", i)
                .and(i.group(
                        RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(Any::getItems),
                        RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(Any::getBlocks)
                )).apply(i, Any::new)
        );

        @Getter
        private final HolderSet<Item> items;
        @Getter
        private final HolderSet<Block> blocks;

        public Any(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Item> items, HolderSet<Block> blocks) {
            super(GoalOptionSupplier.NONE, GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        private final InListAcceptanceCondition<BlockState, Block> condition = InListAcceptanceCondition.block(blocks.stream().map(Holder::value).toList());
                        private final InListAcceptanceCondition<ItemStack, Item> itemCondition = InListAcceptanceCondition.item(items.stream().map(Holder::value).toList());

                        @Override
                        public boolean test(BlockUtil.UsedItemOnBlock value, ServerPlayer player) {
                            return itemCondition.test(value.item(), player) && condition.test(value.block(), player);
                        }

                        @Override
                        public String getId() {
                            return itemCondition.getId() + "_ON_" + condition.getId();
                        }

                        @Override
                        public String getName() {
                            return itemCondition.getName() + " on " + condition.getId();
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return condition.getExamples().stream().map(c -> new CornerIconTextureExtractor(
                                    c,
                                    new CycleTextureExtractor(itemCondition.getExamples()),
                                    10)
                            ).collect(Collectors.toUnmodifiableList());
                        }
                    }), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.items = items;
            this.blocks = blocks;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

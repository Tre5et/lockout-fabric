package me.marin.lockout.lockout.goal.builder.block;

import com.mojang.serialization.Codec;
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
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public abstract class MineBlockGoalBuilder<T> extends GoalBuilder<BlockUtil.MinedBlock, T> {

    public MineBlockGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, BlockState, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier.map(BlockUtil.MinedBlock::block), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                ItemTextureExtractor.item(Items.IRON_PICKAXE),
        10);
    }

    @Override
    public void reifiedUpdater(BlockUtil.MinedBlock update) {}

    public static class Any extends MineBlockGoalBuilder<Void> {
        public static final MapCodec<Any> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> GoalBuilder.codecBuilder("Mine", instance)
                .and(
                        RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(Any::getBlocks)
                ).apply(instance, Any::new));

        @Getter
        private final HolderSet<Block> blocks;

        public Any(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Block> blocks) {
            super(GoalOptionSupplier.NONE, GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.block(blocks.stream().map(Holder::value).toList())), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.blocks = blocks;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class Unique extends MineBlockGoalBuilder<Void> {
        public static final MapCodec<Unique> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> GoalBuilder.codecBuilder("Mine", instance)
                .and(instance.group(
                        RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(Unique::getBlocks),
                        Codec.INT.fieldOf("count").forGetter(Unique::getCount)
                )).apply(instance, Unique::new));

        @Getter
        private final HolderSet<Block> blocks;
        @Getter
        private final int count;

        public Unique(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Block> blocks, int count) {
            super(GoalOptionSupplier.NONE, GoalProgressSupplier.unique("Blocks mined", _ -> InListAcceptanceCondition.block(blocks.stream().map(Holder::value).toList())).mapCreation(_ -> count), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.blocks = blocks;
            this.count = count;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

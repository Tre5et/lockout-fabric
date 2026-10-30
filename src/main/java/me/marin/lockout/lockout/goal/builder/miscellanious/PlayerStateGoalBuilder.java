package me.marin.lockout.lockout.goal.builder.miscellanious;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public abstract class PlayerStateGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick, T> {

    public PlayerStateGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ServerPlayer, ?> progressSupplier, String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, GoalProgressSupplier.player(progressSupplier), namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    public static class EmptyHungerBar extends PlayerStateGoalBuilder<Void> {
        public static final MapCodec<EmptyHungerBar> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .apply(i, EmptyHungerBar::new));

        public EmptyHungerBar(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(ServerPlayer value, ServerPlayer player) {
                            return value.getFoodData().getFoodLevel() == 0;
                        }

                        @Override
                        public String getId() {
                            return "HUNGER_EMPTY";
                        }

                        @Override
                        public String getName() {
                            return "Empty Hunger Bar";
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(new StackingTextureExtractor(List.of(
                                    SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/food_empty_hunger")),
                                    SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/food_half_hunger"))
                            ), 0));
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class HeightAbove extends PlayerStateGoalBuilder<Void> {
        public static final MapCodec<HeightAbove> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .and(i.group(
                        Codec.INT.fieldOf("height").forGetter(HeightAbove::getHeight),
                        RegistryCodecs.holder(Registries.DIMENSION).fieldOf("height").forGetter(HeightAbove::getDimension)
                )).apply(i, HeightAbove::new));

        @Getter
        private final int height;
        @Getter
        private final Holder<Level> dimension;

        public HeightAbove(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, int height, Holder<Level> dimension) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                        @Override
                        public boolean test(ServerPlayer value, ServerPlayer player) {
                            return value.getY() > height && value.level().dimension() == dimension.value().dimension();
                        }

                        @Override
                        public String getId() {
                            return "HEIGHT_" + height + "_" + BuilderUtil.identifierToId(dimension.value().dimension().identifier());
                        }

                        @Override
                        public String getName() {
                            return "Reach height " + height + " in " + BuilderUtil.identifierToName(dimension.value().dimension().identifier());
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return List.of(
                                    new CornerIconTextureExtractor(
                                            GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/geyser_plume_04.png")),
                                            GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/up_arrow.png")),
                                            8)
                            );
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.height = height;
            this.dimension = dimension;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class SleepIn extends PlayerStateGoalBuilder<Void> {
        public static final MapCodec<SleepIn> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("", i)
                .and(RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(SleepIn::getBlocks))
                .apply(i, SleepIn::new));

        @Getter
        private final HolderSet<Block> blocks;

        public SleepIn(String namePrefix, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, HolderSet<Block> blocks) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<ServerPlayer>() {
                        private final InListAcceptanceCondition<BlockState, Block> condition = InListAcceptanceCondition.block(blocks.stream().map(Holder::value).toList());

                        @Override
                        public boolean test(ServerPlayer value, ServerPlayer player) {
                            if(!value.isSleeping() || value.getSleepingPos().isEmpty()) return false;
                            BlockState block = value.level().getBlockState(value.getSleepingPos().get());
                            return condition.test(block, player);
                        }

                        @Override
                        public String getId() {
                            return "SLEEP_" + condition.getId();
                        }

                        @Override
                        public String getName() {
                            return "Sleep in " + condition.getName();
                        }

                        @Override
                        public List<TextureExtractor> getExamples() {
                            return condition.getExamples();
                        }
                    }),
                    namePrefix, difficultySupplier, requirements, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.blocks = blocks;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

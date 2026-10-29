package me.marin.lockout.lockout.goal.builder.inventory;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public abstract class UpdateInventoryGoalBuilder<T,H> extends GoalBuilder<InventoryUtil.UpdatedInventory<H>, T> {

    public UpdateInventoryGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, InventoryUtil.UpdatedInventory<H>, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        super(optionSupplier, progressSupplier, idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
    }

    @Override
    public void reifiedUpdater(InventoryUtil.UpdatedInventory<H> update) {}

    public static class FillBlock extends UpdateInventoryGoalBuilder<Void, BlockState> {
        public static final MapCodec<FillBlock> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("INVENTORY", "Fill", i)
                .and(RegistryCodecs.holder(Registries.BLOCK).fieldOf("block").forGetter(FillBlock::getBlock))
                .apply(i, FillBlock::new));

        @Getter
        private final Holder<Block> block;

        public FillBlock(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<Block> block) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                                @Override
                                public boolean test(InventoryUtil.UpdatedInventory<BlockState> value, ServerPlayer player) {
                                    return value.inventoryHolder() instanceof BlockState && value.inventoryHolder().getBlock().equals(block.value()) &&
                                            value.items().stream().allMatch(s -> !s.isEmpty() && s.count() == value.maxSlotSize());
                                }

                                @Override
                                public String getId() {
                                    return "FULL_" + ItemUtil.getItemId(block.value().asItem());
                                }

                                @Override
                                public String getName() {
                                    return ItemUtil.getItemName(block.value().asItem());
                                }

                                @Override
                                public List<TextureExtractor> getExamples() {
                                    return List.of(ItemUtil.getItemTextureExtractor(block.value().asItem()));
                                }
                            }
                    ),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.block = block;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }

    public static class FillEntity extends UpdateInventoryGoalBuilder<Void, EntityType<?>> {
        public static final MapCodec<FillEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> GoalBuilder.codecBuilder("INVENTORY", "Fill", i)
                .and(RegistryCodecs.holder(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(FillEntity::getEntity))
                .apply(i, FillEntity::new));

        @Getter
        private final Holder<EntityType<?>> entity;

        public FillEntity(String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled, Holder<EntityType<?>> entity) {
            super(GoalOptionSupplier.NONE,
                    GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                                @Override
                                public boolean test(InventoryUtil.UpdatedInventory<EntityType<?>> value, ServerPlayer player) {
                                    return value.inventoryHolder() instanceof EntityType<?> && value.inventoryHolder().equals(entity) &&
                                            value.items().stream().allMatch(s -> !s.isEmpty() && s.count() == value.maxSlotSize());
                                }

                                @Override
                                public String getId() {
                                    return "FULL_" + EntityUtil.getEntityId(entity.value());
                                }

                                @Override
                                public String getName() {
                                    return EntityUtil.getEntityName(entity.value());
                                }

                                @Override
                                public List<TextureExtractor> getExamples() {
                                    return List.of(EntityUtil.getEntityTextureExtractor(entity.value()));
                                }
                            }
                    ),
                    idPrefix, namePrefix, category, difficultySupplier, requirements, customIdProvider, customNameProvider, customTextureExtractorProvider, groups, hints, enabled);
            this.entity = entity;
        }

        @Override
        public MapCodec<? extends GoalBuilder<?, ?>> codec() {
            return MAP_CODEC;
        }
    }
}

package me.marin.lockout.lockout.goal.builder.inventory;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class UpdateInventoryGoalBuilder<T,H> extends GoalBuilder<InventoryUtil.UpdatedInventory<H>, T> {
    public UpdateInventoryGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, InventoryUtil.UpdatedInventory<H>, ?> progressSupplier) {
        super("INVENTORY", "", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(InventoryUtil.UpdatedInventory<H> update) {}

    public static UpdateInventoryGoalBuilder<Void, BlockState> fillBlock(Block block) {
        return new UpdateInventoryGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                            @Override
                            public boolean test(InventoryUtil.UpdatedInventory<BlockState> value, ServerPlayer player) {
                                return value.inventoryHolder() instanceof BlockState && value.inventoryHolder().getBlock().equals(block) &&
                                        value.items().stream().allMatch(s -> !s.isEmpty() && s.count() == value.maxSlotSize());
                            }

                            @Override
                            public String getId() {
                                return ItemUtil.getItemId(block.asItem()) + "_FULL";
                            }

                            @Override
                            public String getName() {
                                return "Fill " + ItemUtil.getItemName(block.asItem());
                            }

                            @Override
                            public List<TextureExtractor> getExamples() {
                                return List.of(ItemUtil.getItemTextureExtractor(block.asItem()));
                            }
                        }
                )
        );
    }

    public static UpdateInventoryGoalBuilder<Void, EntityType<?>> fillEntity(EntityType<?> entity) {
        return new UpdateInventoryGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                            @Override
                            public boolean test(InventoryUtil.UpdatedInventory<EntityType<?>> value, ServerPlayer player) {
                                return value.inventoryHolder() instanceof EntityType<?> && value.inventoryHolder().equals(entity) &&
                                        value.items().stream().allMatch(s -> !s.isEmpty() && s.count() == value.maxSlotSize());
                            }

                            @Override
                            public String getId() {
                                return EntityUtil.getEntityId(entity) + "_FULL";
                            }

                            @Override
                            public String getName() {
                                return "Fill " + EntityUtil.getEntityName(entity);
                            }

                            @Override
                            public List<TextureExtractor> getExamples() {
                                return List.of(EntityUtil.getEntityTextureExtractor(entity));
                            }
                        }
                )
        );
    }
}

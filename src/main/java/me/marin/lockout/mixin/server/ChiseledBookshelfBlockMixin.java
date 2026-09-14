package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChiseledBookShelfBlock.class)
public class ChiseledBookshelfBlockMixin {

    @Inject(method = "useItemOn", at = @At("RETURN"))
    public void onUseWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        LockoutServer.updateLockout(player, _ -> {
            if(!(cir.getReturnValue() instanceof InteractionResult.Success)) return null;
            ChiseledBookShelfBlockEntity blockEntity = (ChiseledBookShelfBlockEntity) world.getBlockEntity(pos);
            return new InventoryUtil.UpdatedInventory<>(blockEntity.getBlockState(), blockEntity.getItems(), 1);
        });
    }

}

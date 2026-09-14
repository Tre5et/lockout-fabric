package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DecoratedPotBlock.class)
public class DecoratedPotBlockMixin {
    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/DecoratedPotBlockEntity;setChanged()V"))
    public void onAddItem(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        LockoutServer.updateLockout(player, _ -> {
            DecoratedPotBlockEntity blockEntity = (DecoratedPotBlockEntity) level.getBlockEntity(pos);
            return new InventoryUtil.UpdatedInventory<>(state, List.of(blockEntity.getTheItem()), blockEntity.getTheItem().getMaxStackSize());
        });
    }
}

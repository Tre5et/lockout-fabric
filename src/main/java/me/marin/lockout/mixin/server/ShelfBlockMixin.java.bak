package me.marin.lockout.mixin.server;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.goals.misc.FillShelfGoal;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShelfBlock.class)
public class ShelfBlockMixin {

    @Inject(method = "useItemOn", at = @At("RETURN"))
    public void onUseWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) return;
        
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        ShelfBlockEntity blockEntity = (ShelfBlockEntity) world.getBlockEntity(pos);
        if (blockEntity == null) return;
        
        if (!cir.getReturnValue().consumesAction()) return;

        lockout$checkShelfFilled(lockout, player, blockEntity);
    }

    @Unique
    private static void lockout$checkShelfFilled(Lockout lockout, Player player, ShelfBlockEntity blockEntity) {
        // Check if all 3 slots of the shelf are filled
        boolean allSlotsFilled = true;
        
        for (int i = 0; i < 3; i++) {
            if (blockEntity.getItem(i).isEmpty()) {
                allSlotsFilled = false;
                break;
            }
        }

        if (!allSlotsFilled) return;
        
        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof FillShelfGoal) {
                lockout.completeGoal(goal, player);
                return;
            }
        }
    }
}



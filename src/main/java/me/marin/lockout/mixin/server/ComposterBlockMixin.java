package me.marin.lockout.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void addItem(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult, CallbackInfoReturnable<BlockState> ci, @Local(name = "fillLevel") int fillLevel, @Local(name = "newState") BlockState newState) {
        LockoutServer.updateLockout(player, _ -> new ItemUtil.CompostedItem(itemStack));
        if(newState != state) LockoutServer.updateLockout(player, _ -> new InventoryUtil.UpdatedInventory<>(state, List.of(itemStack.copyWithCount(fillLevel + 1)), 7));
    }

/*    @Inject(method = "extractProduce", at = @At("RETURN"))
    private static void emptyFullComposterMixin(Entity user, BlockState state, Level world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (user.level().isClientSide()) return;
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        if (!(user instanceof Player player)) return;

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof UseComposterGoal) {
                lockout.completeGoal(goal, player);
            }
        }
    }*/

}

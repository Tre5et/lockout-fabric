package me.marin.lockout.mixin.server;

import me.marin.lockout.CompassItemHandler;
import me.marin.lockout.Lockout;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.CompassItem.class)
public class CompassItemUseOnBlockMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos blockPos = context.getClickedPos();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // Only check if it's a lodestone and we're in a lockout game
        if (!world.getBlockState(blockPos).is(Blocks.LODESTONE)) return;
        if (world.isClientSide()) return;
        if (player == null) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        if (!lockout.isLockoutPlayer(player.getUUID())) return;

        // Check if this is a tracking compass
        if (CompassItemHandler.isCompass(itemStack)) {
            // Block tracking compasses from being used on lodestones entirely
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        // Regular compasses proceed normally - they can be used on lodestones
        // and will complete the advancement goal through the normal advancement system
    }
}

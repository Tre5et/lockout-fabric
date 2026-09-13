package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.block.BlockUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.GlowInkSacItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowInkSacItem.class)
public class GlowInkSacItemMixin {

    @Inject(method="tryApplyToSign", at = @At("RETURN"))
    public void useOnSign(Level level, SignBlockEntity sign, SignTextSlot slot, ItemStack item, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.level().isClientSide()) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;
        if (!cir.getReturnValue()) return;

        lockout.getBoard().update(new BlockUtil.UsedItemOnBlock(item, sign.getBlockState()), player);
    }

}

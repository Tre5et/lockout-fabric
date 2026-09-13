package me.marin.lockout.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.block.BlockUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public class BrushableBlockEntityMixin {

    @Inject(method = "brushingCompleted", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public void finishBrushing(ServerLevel world, LivingEntity brusher, ItemStack brush, CallbackInfo ci, @Local(name = "blockState") BlockState blockState) {
        if (brusher instanceof Player player)
        {
            if (player.level().isClientSide()) return;
            ServerLockoutGame lockout = LockoutServer.lockout;
            if (!LockoutGame.isActive(lockout)) return;

            lockout.getBoard().update(new BlockUtil.UsedItemOnBlock(brush, blockState), player);
        }
    }

}

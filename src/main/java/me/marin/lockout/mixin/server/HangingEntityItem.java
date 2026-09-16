package me.marin.lockout.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import me.marin.lockout.lockout.goal.builder.block.BlockUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.HangingEntityItem.class)
public class HangingEntityItem {

    @Shadow @Final private EntityType<? extends HangingEntity> type;

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "itemInHand") ItemStack itemInHand) {
        LockoutServer.updateLockout(context.getPlayer(), _ -> {
            BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
            return new BlockUtil.UsedItemOnBlock(itemInHand, blockState);
        });
    }

}

package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.block.BlockUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
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

    @Inject(method = "useOn", at = @At("RETURN"))
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        LockoutServer.updateLockout(context.getPlayer(), _ -> {
            if (cir.getReturnValue() != InteractionResult.SUCCESS) return null;
            BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
            return new BlockUtil.UsedItemOnBlock(context.getItemInHand(), blockState);
        });
    }

}

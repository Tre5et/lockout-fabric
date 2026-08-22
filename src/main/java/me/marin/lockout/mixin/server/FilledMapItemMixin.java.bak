package me.marin.lockout.mixin.server;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.goals.misc.MapBannerWaypointGoal;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItem.class)
public class FilledMapItemMixin {

    @Inject(method = "useOn", at = @At("RETURN"))
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.SUCCESS) return;
        Player player = context.getPlayer();
        if (player == null) return;
        if (context.getLevel().isClientSide()) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            for (Goal goal : lockout.getBoard().getGoals()) {
                if (goal == null) continue;
                if (goal.isCompleted()) continue;

                if (goal instanceof MapBannerWaypointGoal) {
                    lockout.completeGoal(goal, player);
                }
            }
        }
    }

}

package me.marin.lockout.mixin.server;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.goals.misc.PlacePaintingGoal;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
public class DecorationItemMixin {

    @Shadow @Final private EntityType<? extends HangingEntity> type;

    @Inject(method = "useOn", at = @At("RETURN"))
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide()) return;
        if (cir.getReturnValue() != InteractionResult.SUCCESS) return;
        if (this.type != EntityTypes.PAINTING) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        Player player = context.getPlayer();

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof PlacePaintingGoal) {
                lockout.completeGoal(goal, player);
                return;
            }
        }
    }

}

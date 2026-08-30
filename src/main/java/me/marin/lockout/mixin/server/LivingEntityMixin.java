package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.break_item.BreakItemGoalBuilder;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /*@Inject(method = "hurtServer", at = @At("RETURN"))
    public void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        if (!(source.getEntity() instanceof Player player) || !cir.getReturnValue()) return;
        if (player.level().isClientSide()) return;

        if (!lockout.isLockoutPlayer(player.getUUID())) return;
        LockoutTeamServer team = (LockoutTeamServer) lockout.getPlayerTeam(player.getUUID());
        lockout.damageDealt.putIfAbsent(team, 0d);
        lockout.damageDealt.merge(team, (double)amount, Double::sum);

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof Deal400DamageGoal deal400DamageGoal) {
                team.sendTooltipUpdate(deal400DamageGoal);
                if (lockout.damageDealt.get(team) >= 400) {
                    lockout.completeGoal(goal, player);
                }
            }
        }
    }*/

    @Inject(method = "onEquippedItemBroken", at = @At("HEAD"))
    public void onEquipmentBreak(Item item, EquipmentSlot slot, CallbackInfo ci) {
        if (!((Object)this instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        lockout.getBoard().update(new BreakItemGoalBuilder.BrokenItem(item), player);
    }

}

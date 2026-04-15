package me.marin.lockout.mixin.server;

import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeamServer;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.goals.misc.Deal400DamageGoal;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.marin.lockout.lockout.goals.misc.BreakToolGoal;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
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
    }

    @Inject(method = "onEquippedItemBroken", at = @At("HEAD"))
    public void onEquipmentBreak(Item item, EquipmentSlot slot, CallbackInfo ci) {
        if (!((Object)this instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        ItemStack stack = item.getDefaultInstance();

        // Check if it's a tool (has TOOL component or is damageable)
        if (stack.has(DataComponents.TOOL) || stack.getMaxDamage() > 0) {
            for (Goal goal : lockout.getBoard().getGoals()) {
                if (goal == null) continue;
                if (goal.isCompleted()) continue;

                if (goal instanceof BreakToolGoal) {
                    lockout.completeGoal(goal, player);
                }
            }
        }
    }

}

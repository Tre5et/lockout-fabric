package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.statistic.StatisticUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

/*    @Inject(method = "touch", at = @At("HEAD"))
    public void onCollide(Entity entity, CallbackInfo ci) {
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof OpponentHitBySnowballGoal) {
                if (entity instanceof Snowball snowballEntity) {
                    if (snowballEntity.getOwner() instanceof Player shooter && !Objects.equals(player, shooter)) {
                        lockout.completeMultiOpponentGoal(goal, player, player.getName().getString() + " hit by " + shooter.getName().getString() + " with a Snowball.");
                    }
                }
            }
            if (goal instanceof OpponentHitByEggGoal) {
                if (entity instanceof ThrownEgg snowballEntity) {
                    if (snowballEntity.getOwner() instanceof Player shooter && !Objects.equals(player, shooter)) {
                        lockout.completeMultiOpponentGoal(goal, player, player.getName().getString() + " hit by " + shooter.getName().getString() + " with an Egg.");
                    }
                }
            }
        }
    }*/

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void onHurtServer(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (lockout == null) return;
        if (world.isClientSide()) return;
        if (!lockout.getState().isActive()) {
            cir.setReturnValue(false);
        }
    }

    /*@Inject(method = "hurtServer", at = @At("RETURN"))
    public void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        if (!cir.getReturnValue()) return;

        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        if (!lockout.isLockoutPlayer(player.getUUID())) return;
        LockoutTeamServer team = (LockoutTeamServer) lockout.getPlayerTeam(player.getUUID());

        lockout.damageTaken.putIfAbsent(team, 0d);
        lockout.damageTaken.merge(team, (double)amount, Double::sum);

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof Take200DamageGoal take200DamageGoal) {
                team.sendTooltipUpdate(take200DamageGoal);
                if (lockout.damageTaken.get(team) >= 200) {
                    lockout.completeGoal(goal, team);
                }
            }
            if (goal instanceof OpponentTakesFallDamageGoal) {
                if (source.is(DamageTypes.FALL)) {
                    lockout.completeMultiOpponentGoal(goal, player, player.getName().getString() + " took fall damage.");
                }
            }
            if (goal instanceof OpponentHitByArrowGoal) {
                if (source.getDirectEntity() instanceof Arrow arrowEntity) {
                    if (arrowEntity.getOwner() instanceof Player shooter && !Objects.equals(player, shooter)) {
                        lockout.completeMultiOpponentGoal(goal, player, player.getName().getString() + " hit by " + shooter.getName().getString() + " with an Arrow.");
                    }
                }
            }
            if (goal instanceof OpponentTakes100DamageGoal) {
                if (lockout.damageTaken.get(team) >= 100) {
                    lockout.completeMultiOpponentGoal(goal, team, team.getDisplayName() + " took 100 damage.");
                }
            }
        }
    }*/

    @Inject(method = "awardStat(Lnet/minecraft/resources/Identifier;)V", at = @At("HEAD"))
    public void onIncrementStat(Identifier stat, CallbackInfo ci) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        lockout.getBoard().update(new StatisticUtil.StatisticChanged(stat, 1), player);
    }

    @Inject(method = "awardStat(Lnet/minecraft/resources/Identifier;I)V", at = @At("HEAD"))
    public void onIncreaseStat(Identifier stat, int amount, CallbackInfo ci) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        lockout.getBoard().update(new StatisticUtil.StatisticChanged(stat, amount), player);
    }

    /*@Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    public void onExperienceLevelUp(int levels, CallbackInfo ci) {
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof ReachXPLevelGoal reachXPLevelGoal) {
                if (player.experienceLevel >= reachXPLevelGoal.getAmount()) {
                    lockout.completeGoal(goal, player);
                }
            }
        }
    }

    @Inject(method = "blockUsingItem", at = @At(value = "TAIL"))
    public void onTakeShieldHit(ServerLevel world, LivingEntity attacker, DamageSource source, float damage, CallbackInfo ci) {
        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;

        float f = attacker.getSecondsToDisableBlocking();

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;
            if (f <= 0.0F) continue;

            if (goal instanceof HaveShieldDisabledGoal) {
                lockout.completeGoal(goal, player);
            }
        }
    }*/


}

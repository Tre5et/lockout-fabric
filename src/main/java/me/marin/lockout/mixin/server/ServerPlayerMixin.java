package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.experience.ExperienceUtils;
import me.marin.lockout.lockout.goal.builder.statistic.StatisticUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("TAIL"))
    public void onAwardStat(Stat<?> stat, int count, CallbackInfo ci) {
        LockoutServer.updateLockout((ServerPlayer)(Object)this, _ -> new StatisticUtil.StatisticChanged(stat, count));
    }

    @Inject(method = "giveExperienceLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    public void onExperienceLevelUp(int levels, CallbackInfo ci) {
        LockoutServer.updateLockout((ServerPlayer)(Object)this, p -> new ExperienceUtils.ReachedExperienceLevel(p.experienceLevel));
    }
}

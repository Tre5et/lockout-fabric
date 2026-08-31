package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.damage.DamageUtil;
import me.marin.lockout.lockout.goal.builder.item.BreakItemGoalBuilder;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    public void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;
        if (!(source.getEntity() instanceof Player player) || !cir.getReturnValue()) return;
        if (player.level().isClientSide()) return;

        lockout.getBoard().update(new DamageUtil.DealtDamage(amount), player);
    }

    @Inject(method = "onEquippedItemBroken", at = @At("HEAD"))
    public void onEquipmentBreak(Item item, EquipmentSlot slot, CallbackInfo ci) {
        if (!((Object)this instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        lockout.getBoard().update(new BreakItemGoalBuilder.BrokenItem(item), player);
    }

}

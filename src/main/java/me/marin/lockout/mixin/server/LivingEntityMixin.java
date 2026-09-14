package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.damage.DamageUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    public void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LockoutServer.updateLockout(source.getEntity(), _ -> {
            if(!cir.getReturnValue()) return null;
            return new DamageUtil.DealtDamage(amount);
        });
    }

    @Inject(method = "onEquippedItemBroken", at = @At("HEAD"))
    public void onEquipmentBreak(ItemStack brokenItem, EquipmentSlot inSlot, CallbackInfo ci) {
        LockoutServer.updateLockout((LivingEntity)(Object)this, _ -> new ItemUtil.BrokenItem(brokenItem.getItem()));
    }

}

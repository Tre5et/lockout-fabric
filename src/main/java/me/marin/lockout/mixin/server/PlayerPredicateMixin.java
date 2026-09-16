package me.marin.lockout.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.advancements.predicates.entity.PlayerPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerPredicate.class)
public class PlayerPredicateMixin {
    @Inject(method = "matches", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/phys/EntityHitResult;getEntity()Lnet/minecraft/world/entity/Entity;"))
    public void onSpyglassAtEntity(Entity entity, ServerLevel level, Vec3 position, CallbackInfoReturnable<Boolean> cir, @Local(name = "lookingAtEntity") Entity lookingAtEntity) {
        LockoutServer.updateLockout(entity, p -> {
            if(!p.hasLineOfSight(lookingAtEntity)) return null;
            return new EntityUtil.UsedItemOnEntity(p.getActiveItem(), lookingAtEntity.getType());
        });
    }
}

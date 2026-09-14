package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin {
    @Inject(method = "setPersistentAngerTarget", at = @At("HEAD"))
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> angryAt, CallbackInfo ci) {
        LockoutServer.updateLockout(angryAt.getEntity(((ZombifiedPiglin)(Object)this).level(), LivingEntity.class), _ -> new EntityUtil.AngeredEntity(EntityTypes.ZOMBIFIED_PIGLIN));
    }
}

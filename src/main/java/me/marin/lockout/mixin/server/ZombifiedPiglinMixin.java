package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
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
        if(!((Object)this instanceof ZombifiedPiglin piglin)) return;
        if (piglin.level().isClientSide()) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        try {
            LivingEntity target = angryAt.getEntity(piglin.level(), LivingEntity.class);
            if (target instanceof ServerPlayer serverPlayer) {
                lockout.getBoard().update(new EntityUtil.AngeredEntity(piglin.getType()), serverPlayer);
            }
        } catch (Exception ignored) {}
    }

}

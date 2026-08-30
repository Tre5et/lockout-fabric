package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.advancements.triggers.BredAnimalsTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BredAnimalsTrigger.class)
public class BredAnimalsTriggerMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    public void onBreedAnimal(ServerPlayer player, Animal parent, Animal partner, @Nullable AgeableMob child, CallbackInfo ci) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        lockout.getBoard().update(new EntityUtil.BredEntity(parent.getType()), player);
    }

}

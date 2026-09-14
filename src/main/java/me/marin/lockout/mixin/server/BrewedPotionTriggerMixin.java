package me.marin.lockout.mixin.server;

import me.marin.lockout.server.LockoutServer;
import net.minecraft.advancements.triggers.BrewedPotionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewedPotionTrigger.class)
public class BrewedPotionTriggerMixin {

    @Inject(method = "trigger", at = @At("HEAD"))
    public void onTrigger(ServerPlayer player, PotionContents potion, CallbackInfo ci) {
        LockoutServer.updateLockout(player, _ -> potion);
    }

}

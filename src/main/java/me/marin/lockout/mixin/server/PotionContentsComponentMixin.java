package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionContents.class)
public class PotionContentsComponentMixin {

    @Inject(method = "onConsume", at = @At("HEAD"))
    public void onConsume(Level world, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo ci) {
        if (!(user instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        lockout.getBoard().update(new ItemUtil.ConsumedItem(stack), player);
    }

}

package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public class FoodComponentMixin {

    @Inject(method = "onConsume", at = @At("HEAD"))
    public void onConsume(Level world, LivingEntity user, ItemStack itemStack, Consumable consumable, CallbackInfo ci) {
        if (world.isClientSide()) return;
        if (!(user instanceof Player player)) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        lockout.getBoard().update(new ItemUtil.ConsumedItem(itemStack), player);
    }
}

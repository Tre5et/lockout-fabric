package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandMenu.PotionSlot.class)
public class BrewingStandScreenHandlerPotionSlotMixin {

    @Inject(method = "onTake", at = @At("TAIL"))
    public void onTakeItem(Player player, ItemStack stack, CallbackInfo ci) {
        LockoutServer.updateLockout(player, _ -> new ItemUtil.BrewedItem(stack));
    }

}

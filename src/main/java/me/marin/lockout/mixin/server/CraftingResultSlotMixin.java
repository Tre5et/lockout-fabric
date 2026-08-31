package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow @Final
    private Player player;

    @Shadow
    private int removeCount;

    @Inject(method = "checkTakeAchievements(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    public void onCraft(ItemStack stack, CallbackInfo ci) {
        if (player.level().isClientSide()) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        if (removeCount < 0 || stack.isEmpty()) {
            return;
        }

        if (!(player.containerMenu instanceof CraftingMenu || player.containerMenu instanceof InventoryMenu)) return;

        lockout.getBoard().update(new ItemUtil.CraftedItem(stack.getItem()), player);
    }

}

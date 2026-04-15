package me.marin.lockout.mixin.server;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.Goal;
import me.marin.lockout.lockout.goals.misc.FillBundleGoal;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public class BundleItemMixin {

    @Inject(method = "overrideOtherStackedOnMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;broadcastChangesOnContainerMenu(Lnet/minecraft/world/entity/player/Player;)V"))
    public void onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (player.level().isClientSide()) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        lockout$onClick(lockout, player, stack);
    }

    @Inject(method = "overrideStackedOnOther", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;broadcastChangesOnContainerMenu(Lnet/minecraft/world/entity/player/Player;)V"))
    public void onStackClicked(ItemStack stack, Slot slot, ClickAction clickType, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.level().isClientSide()) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        lockout$onClick(lockout, player, stack);
    }

    @Unique
    private static void lockout$onClick(Lockout lockout, Player player, ItemStack stack) {
        BundleContents bcc = stack.get(DataComponents.BUNDLE_CONTENTS);

        // Check if bundle is completely full (no space left)
        if (bcc.weight().compareTo(Fraction.ONE) < 0) {
            return;
        }

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof FillBundleGoal) {
                lockout.completeGoal(goal, player);
            }
        }
    }

}

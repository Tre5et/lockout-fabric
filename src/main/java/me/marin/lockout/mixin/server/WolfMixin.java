package me.marin.lockout.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public class WolfMixin {

    @Inject(method = "mobInteract", at = @At(value = "RETURN"))
    public void onEquipArmor(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "itemStack") ItemStack itemStack) {
        LockoutServer.updateLockout(player, _ -> new EntityUtil.UsedItemOnEntity(itemStack, ((Wolf)(Object)this).getType()));
    }

}

package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    @Inject(method = "placeFood", at = @At("RETURN"))
    public void addItem(ServerLevel world, LivingEntity entity, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        LockoutServer.updateLockout(entity, _ -> {
            CampfireBlockEntity campfire = (CampfireBlockEntity) (Object) this;
            return new InventoryUtil.UpdatedInventory<>(campfire.getBlockState(), campfire.getItems(), 1);
        });
    }

}

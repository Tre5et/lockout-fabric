package me.marin.lockout.mixin.server;

import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.server.LockoutServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ArmorStand.class)
public class ArmorStandMixin {

    @Inject(method = "interact", at = @At("RETURN"))
    public void onInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        LockoutServer.updateLockout(player, p -> {
            if (p.gameMode.getGameModeForPlayer() != GameType.SURVIVAL || !(cir.getReturnValue() instanceof InteractionResult.Success)) return null;
            ArmorStand armorStand = (ArmorStand) (Object) this;
            List<ItemStack> armor = new ArrayList<>();
            armor.add(armorStand.getItemBySlot(EquipmentSlot.HEAD));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.CHEST));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.LEGS));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.FEET));
            return new InventoryUtil.UpdatedInventory<>(EntityTypes.ARMOR_STAND, armor, 1);
        });
    }

}

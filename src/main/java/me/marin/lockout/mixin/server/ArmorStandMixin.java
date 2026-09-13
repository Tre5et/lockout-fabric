package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.inventory.InventoryUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.server.level.ServerPlayer;
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

@Mixin(ArmorStand.class)
public class ArmorStandMixin {

    @Inject(method = "interact", at = @At("RETURN"))
    public void onInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide()) return;
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ArmorStand armorStand = (ArmorStand) (Object) this;

        if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && cir.getReturnValue() == InteractionResult.SUCCESS_SERVER) {
            var armor = new ArrayList<ItemStack>();
            armor.add(armorStand.getItemBySlot(EquipmentSlot.HEAD));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.CHEST));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.LEGS));
            armor.add(armorStand.getItemBySlot(EquipmentSlot.FEET));

            lockout.getBoard().update(new InventoryUtil.UpdatedInventory<>(EntityTypes.ARMOR_STAND, armor, 1), player);
        }
    }

}

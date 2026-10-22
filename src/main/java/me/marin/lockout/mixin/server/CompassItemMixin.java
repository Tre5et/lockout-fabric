package me.marin.lockout.mixin.server;

import me.marin.lockout.CompassItemHandler;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(CompassItem.class)
public class CompassItemMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    public void onInventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        LockoutServer.updateLockout(entity, p -> {
            if (CompassItemHandler.isPlayerCompass(stack)) {
                int selectionNum = LockoutServer.compassHandler.currentSelection.getOrDefault(p.getUUID(), -1);
                if (selectionNum < 0) return null;
                UUID selectedId = LockoutServer.compassHandler.players.get(selectionNum);
                Player selectedPlayer = world.getServer().getPlayerList().getPlayer(selectedId);
                if (selectedPlayer != null) {
                    if (selectedPlayer.level().equals(p.level())) {
                        stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(world.dimension(), selectedPlayer.blockPosition())), true));
                        ci.cancel();
                    }
                } else {
                    stack.remove(DataComponents.LODESTONE_TRACKER);
                }

                CompassItemHandler cih = LockoutServer.compassHandler;
                String trackingPlayerName = cih.playerNames.get(cih.players.get(cih.currentSelection.get(p.getUUID())));

                stack.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(ChatFormatting.RESET + "Tracking: " + trackingPlayerName));
            }
            return null;
        });
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos blockPos = context.getClickedPos();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        // Only check if it's a lodestone and we're in a lockout game

        if (!world.getBlockState(blockPos).is(Blocks.LODESTONE)) return;
        if (world.isClientSide()) return;
        if (player == null) return;

        ServerLockoutGame lockout = LockoutServer.lockout;
        if (!LockoutGame.isActive(lockout)) return;
        if (!lockout.isLockoutPlayer(player.getUUID())) return;

        // Check if this is a tracking compass
        if (CompassItemHandler.isPlayerCompass(itemStack) || CompassItemHandler.isHintCompass(itemStack)) {
            // Block tracking compasses from being used on lodestones entirely
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        // Regular compasses proceed normally - they can be used on lodestones
        // and will complete the advancement goal through the normal advancement system
    }

    @Redirect(method = "isFoil", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
    public boolean isFoil(ItemStack instance, DataComponentType<?> dataComponentType) {
        if(CompassItemHandler.isHintCompass(instance)) return false;
        return instance.has(dataComponentType);
    }

    @Redirect(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
    public boolean getName(ItemStack instance, DataComponentType<?> dataComponentType) {
        if(CompassItemHandler.isHintCompass(instance)) return false;
        return instance.has(dataComponentType);
    }
}

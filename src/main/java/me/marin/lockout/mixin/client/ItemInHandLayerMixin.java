package me.marin.lockout.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.marin.lockout.CompassItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin<S extends ArmedEntityRenderState> {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    public void render(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = arm == player.getMainArm() ? player.getMainHandItem() : player.getOffhandItem();
        if (!CompassItemHandler.isCompass(stack)) return;

        ci.cancel();
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + 0 * -0.6F, -0.72F);
    }

}

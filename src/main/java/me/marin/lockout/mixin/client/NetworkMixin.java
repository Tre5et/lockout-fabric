package me.marin.lockout.mixin.client;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class NetworkMixin {
    private static final Logger LOGGER = LogManager.getLogger("lockout");

    @Inject(
            method = "onPacketError",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPacketException(Packet<?> packet, Exception exception, CallbackInfo ci) {
        LOGGER.warn("Strict error handling was triggered, but disconnection was prevented");
        LOGGER.error("Failed to handle packet {}", packet, exception);
        ci.cancel();
    }
}

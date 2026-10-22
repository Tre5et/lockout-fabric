package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.server.ServerConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record ServerConfigPayload(ServerConfig config) implements CustomPacketPayload {
    public static final Type<ServerConfigPayload> ID = new Type<>(Constants.SERVER_CONFIG_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigPayload> CODEC = StreamCodec.composite(
            ServerConfig.CODEC,
            ServerConfigPayload::config,
            ServerConfigPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

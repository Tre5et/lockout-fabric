package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestHintPayload(String goalId, int hintIndex) implements CustomPacketPayload {
    public static final Type<RequestHintPayload> ID = new Type<>(Constants.REQUEST_HINT_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHintPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RequestHintPayload::goalId,
            ByteBufCodecs.INT,
            RequestHintPayload::hintIndex,
            RequestHintPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

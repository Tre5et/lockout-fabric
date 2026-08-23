package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HintResultPayload(String goalId, int hintIndex, String message, boolean success) implements CustomPacketPayload {
    public static final Type<HintResultPayload> ID = new Type<>(Constants.HINT_RESULT_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, HintResultPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            HintResultPayload::goalId,
            ByteBufCodecs.INT,
            HintResultPayload::hintIndex,
            ByteBufCodecs.STRING_UTF8,
            HintResultPayload::message,
            ByteBufCodecs.BOOL,
            HintResultPayload::success,
            HintResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

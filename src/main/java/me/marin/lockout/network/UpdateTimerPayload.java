package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTimerPayload(long ticks) implements CustomPacketPayload {
    public static final Type<UpdateTimerPayload> ID = new Type<>(Constants.UPDATE_TIMER_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTimerPayload> CODEC = StreamCodec.composite(ByteBufCodecs.LONG, UpdateTimerPayload::ticks, UpdateTimerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

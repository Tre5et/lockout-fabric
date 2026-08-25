package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CompleteTaskPayload(String goal, int teamIndex, String completedName, boolean announce) implements CustomPacketPayload {
    public static final Type<CompleteTaskPayload> ID = new Type<>(Constants.COMPLETE_TASK_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, CompleteTaskPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
            CompleteTaskPayload::goal,
            ByteBufCodecs.INT,
            CompleteTaskPayload::teamIndex,
            ByteBufCodecs.STRING_UTF8,
            CompleteTaskPayload::completedName,
            ByteBufCodecs.BOOL,
            CompleteTaskPayload::announce,
            CompleteTaskPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

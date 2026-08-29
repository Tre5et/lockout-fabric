package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record CompleteTaskPayload(String goal, int teamIndex, Optional<String> completedName, boolean announce) implements CustomPacketPayload {
    public static final Type<CompleteTaskPayload> ID = new Type<>(Constants.COMPLETE_TASK_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, CompleteTaskPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
            CompleteTaskPayload::goal,
            ByteBufCodecs.INT,
            CompleteTaskPayload::teamIndex,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            CompleteTaskPayload::completedName,
            ByteBufCodecs.BOOL,
            CompleteTaskPayload::announce,
            CompleteTaskPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

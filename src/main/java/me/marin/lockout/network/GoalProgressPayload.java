package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record GoalProgressPayload(String goalId, String progress, Optional<Integer> newCompletion) implements CustomPacketPayload {
    public static final Type<GoalProgressPayload> ID = new Type<>(Constants.GOAL_PROGRESS_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, GoalProgressPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            GoalProgressPayload::goalId,
            ByteBufCodecs.STRING_UTF8,
            GoalProgressPayload::progress,
            ByteBufCodecs.optional(ByteBufCodecs.INT),
            GoalProgressPayload::newCompletion,
            GoalProgressPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record HintResultPayload(ResourceKey<GoalBuilder<?,?>> goalId, int hintIndex, Optional<String> data, Optional<String> error) implements CustomPacketPayload {
    public static final Type<HintResultPayload> ID = new Type<>(Constants.HINT_RESULT_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, HintResultPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(LockoutRegistryKeys.GOAL),
            HintResultPayload::goalId,
            ByteBufCodecs.INT,
            HintResultPayload::hintIndex,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            HintResultPayload::data,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            HintResultPayload::error,
            HintResultPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

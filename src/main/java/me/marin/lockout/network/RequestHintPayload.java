package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record RequestHintPayload(ResourceKey<GoalBuilder<?,?>> goalId, int hintIndex) implements CustomPacketPayload {
    public static final Type<RequestHintPayload> ID = new Type<>(Constants.REQUEST_HINT_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHintPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(LockoutRegistryKeys.GOAL),
            RequestHintPayload::goalId,
            ByteBufCodecs.INT,
            RequestHintPayload::hintIndex,
            RequestHintPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

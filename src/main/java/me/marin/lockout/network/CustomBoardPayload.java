package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.BuildData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public record CustomBoardPayload(Optional<List<BuildData>> boardOrClear) implements CustomPacketPayload {
    public static final Type<CustomBoardPayload> ID = new Type<>(Constants.CUSTOM_BOARD_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomBoardPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BuildData.CODEC.apply(ByteBufCodecs.list())),
            CustomBoardPayload::boardOrClear,
            CustomBoardPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

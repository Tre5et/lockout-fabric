package me.marin.lockout.network;

import me.marin.lockout.ChatManager;
import me.marin.lockout.Constants;
import me.marin.lockout.server.ServerConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record UpdateSettingsPayload(
        Optional<Integer> boardSize,
        Optional<Integer> startTime,
        Optional<Boolean> giveCompasses,
        Optional<ServerConfig.HintMode> hintMode,
        Optional<ChatManager.Type> chatMode,
        Optional<Identifier> difficulty
) implements CustomPacketPayload {
    public static final Type<UpdateSettingsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "update_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSettingsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT),
            UpdateSettingsPayload::boardSize,
            ByteBufCodecs.optional(ByteBufCodecs.INT),
            UpdateSettingsPayload::startTime,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL),
            UpdateSettingsPayload::giveCompasses,
            ByteBufCodecs.optional(ServerConfig.HintMode.CODEC),
            UpdateSettingsPayload::hintMode,
            ByteBufCodecs.optional(ChatManager.Type.CODEC),
            UpdateSettingsPayload::chatMode,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            UpdateSettingsPayload::difficulty,
            UpdateSettingsPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static UpdateSettingsPayload boardSize(int boardSize) {
        return new UpdateSettingsPayload(Optional.of(boardSize), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static UpdateSettingsPayload startTime(int startTime) {
        return new UpdateSettingsPayload(Optional.empty(), Optional.of(startTime), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static UpdateSettingsPayload giveCompasses(boolean giveCompasses) {
        return new UpdateSettingsPayload(Optional.empty(), Optional.empty(), Optional.of(giveCompasses), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static UpdateSettingsPayload hintMode(ServerConfig.HintMode hintMode) {
        return new UpdateSettingsPayload(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(hintMode), Optional.empty(), Optional.empty());
    }

    public static UpdateSettingsPayload chatMode(ChatManager.Type chatMode) {
        return new UpdateSettingsPayload(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(chatMode), Optional.empty());
    }

    public static UpdateSettingsPayload difficulty(Identifier difficulty) {
        return new UpdateSettingsPayload(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(difficulty));
    }
}

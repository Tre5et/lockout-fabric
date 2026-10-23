package me.marin.lockout.network;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import me.marin.lockout.Constants;
import me.marin.lockout.game.GameMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record RequestActionPayload(
        Action action,
        Optional<GameMode> mode,
        Optional<String> players,
        Optional<String> teams,
        Optional<Integer> goal
) implements CustomPacketPayload {
    public static final Type<RequestActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "request_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestActionPayload> CODEC = StreamCodec.composite(
            Action.CODEC,
            RequestActionPayload::action,
            ByteBufCodecs.optional(GameMode.CODEC),
            RequestActionPayload::mode,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            RequestActionPayload::players,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            RequestActionPayload::teams,
            ByteBufCodecs.optional(ByteBufCodecs.INT),
            RequestActionPayload::goal,
            RequestActionPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static RequestActionPayload simple(Action action) {
        return new RequestActionPayload(action, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static RequestActionPayload start(GameMode mode, @Nullable String players, @Nullable String teams) {
        return new RequestActionPayload(Action.START, Optional.of(mode), Optional.ofNullable(players), Optional.ofNullable(teams), Optional.empty());
    }

    public static RequestActionPayload goal(Action action, int goalIndex, String teams) {
        return new RequestActionPayload(action, Optional.empty(), Optional.empty(), Optional.of(teams), Optional.of(goalIndex));
    }

    public enum Action {
        GET_CHAT_MODE,
        RELOAD_GOALS,
        START,
        PAUSE,
        UNPAUSE,
        FORFEIT,
        GRANT_GOAL,
        REVOKE_GOAL,
        NEARBY_STRUCTURES,
        NEARBY_BIOMES;

        public static final StreamCodec<ByteBuf, Action> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(Action::name, Action::valueOf));
    }
}

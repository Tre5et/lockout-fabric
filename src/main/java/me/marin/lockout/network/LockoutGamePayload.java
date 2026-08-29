package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.GameState;
import me.marin.lockout.lockout.goal.builder.BuildData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record LockoutGamePayload(List<LockoutTeam> teams, List<BuildData> goals, GameState state) implements CustomPacketPayload {
    public static final Type<LockoutGamePayload> ID = new Type<>(Constants.LOCKOUT_GOALS_TEAMS_PACKET);

    public static final StreamCodec<RegistryFriendlyByteBuf, LockoutGamePayload> CODEC = StreamCodec.composite(
            LockoutTeam.CODEC.apply(ByteBufCodecs.list()),
            LockoutGamePayload::teams,
            BuildData.CODEC.apply(ByteBufCodecs.list()),
            LockoutGamePayload::goals,
            ByteBufCodecs.STRING_UTF8.map(GameState::valueOf, GameState::name),
            LockoutGamePayload::state,
            LockoutGamePayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

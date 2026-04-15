package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record LockoutGoalsTeamsPayload(List<LockoutTeam> teams, List<Pair<Pair<String, String>, Integer>> goals,
                                       boolean isRunning) implements CustomPacketPayload {
    public static final Type<LockoutGoalsTeamsPayload> ID = new Type<>(Constants.LOCKOUT_GOALS_TEAMS_PACKET);

    public static final StreamCodec<RegistryFriendlyByteBuf, LockoutGoalsTeamsPayload> CODEC = new StreamCodec<RegistryFriendlyByteBuf, LockoutGoalsTeamsPayload>() {
        @Override
        public LockoutGoalsTeamsPayload decode(RegistryFriendlyByteBuf buf) {
            // Read teams
            int teamsSize = buf.readInt();
            List<LockoutTeam> teams = new ArrayList<>(teamsSize);
            for (int i = 0; i < teamsSize; i++) {
                int teamSize = buf.readInt();
                ChatFormatting color = ChatFormatting.getByName(buf.readUtf());
                List<String> playerNames = new ArrayList<>();
                List<UUID> playerIds = new ArrayList<>();
                for (int j = 0; j < teamSize; j++) {
                    String playerName = buf.readUtf();
                    UUID playerId = buf.readUUID();
                    playerNames.add(playerName);
                    playerIds.add(playerId);
                }
                teams.add(new LockoutTeam(playerNames, playerIds, color));
            }

            // Read goals
            int size = buf.readInt();
            List<Pair<Pair<String, String>, Integer>> goals = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                goals.add(new Pair<>(new Pair<>(buf.readUtf(), buf.readUtf()), buf.readInt()));
            }

            boolean isRunning = buf.readBoolean();
            return new LockoutGoalsTeamsPayload(teams, goals, isRunning);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, LockoutGoalsTeamsPayload payload) {
            // Write teams
            List<LockoutTeam> teams = payload.teams();
            buf.writeInt(teams.size());
            for (LockoutTeam team : payload.teams()) {
                buf.writeInt(team.getPlayerNames().size());
                buf.writeUtf(team.getColor().getName());
                for (int i = 0; i < team.getPlayerNames().size(); i++) {
                    buf.writeUtf(team.getPlayerNames().get(i));
                    buf.writeUUID(team.getPlayerIds().get(i));
                }
            }

            // Write goals
            buf.writeInt(payload.goals().size());
            for (Pair<Pair<String, String>, Integer> goal : payload.goals()) {
                buf.writeUtf(goal.getA().getA());
                buf.writeUtf(goal.getA().getB());
                buf.writeInt(goal.getB());
            }

            buf.writeBoolean(payload.isRunning);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

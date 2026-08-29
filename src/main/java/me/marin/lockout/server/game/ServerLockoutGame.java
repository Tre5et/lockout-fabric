package me.marin.lockout.server.game;

import com.google.gson.*;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.server.ServerLockoutTeam;
import me.marin.lockout.game.GameState;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.network.EndLockoutPayload;
import me.marin.lockout.network.LockoutGamePayload;
import me.marin.lockout.network.UpdateTimerPayload;
import me.marin.lockout.server.LockoutServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ServerLockoutGame extends LockoutGame<ServerLockoutBoard> {
    public ServerLockoutGame(ServerLockoutBoard board, List<ServerLockoutTeam> teams) {
        super(board, teams);
    }

    public void tick() {
        ticks++;
    }

    public void evaluateWinnerAndEndGame() {
        PlayerList playerManager = LockoutServer.server.getPlayerList();

        List<LockoutTeam> winners = getWinner();
        if(winners.isEmpty()) return;
        setState(GameState.FINISHED);

        if (winners.size() == 1) {
            playerManager.broadcastSystemMessage(Component.literal(winners.getFirst().getDisplayName() + " wins."), false);
        } else {
            playerManager.broadcastSystemMessage(Component.literal("It's a tie! " + getWinnerTeamsString(winners) + " win."), false);
        }

        var payload = new EndLockoutPayload(winners.stream().mapToInt(teams::indexOf).toArray(), System.currentTimeMillis());
        for (ServerPlayer serverPlayer : LockoutServer.server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    @Override
    public List<ServerLockoutTeam> getTeams() {
        return super.getTeams().stream().map(t -> (ServerLockoutTeam)t).toList();
    }

    @Override
    public ServerLockoutTeam getPlayerTeam(UUID playerId) {
        return (ServerLockoutTeam) super.getPlayerTeam(playerId);
    }

    public boolean isLockoutPlayer(Player player) {
        return isLockoutPlayer(player.getUUID());
    }
    public boolean isLockoutPlayer(UUID playerId) {
        for (LockoutTeam team : teams) {
            if (!team.isForfeited() && team.containsPlayer(playerId)) {
                return true;
            }
        }
        return false;
    }

    public List<LockoutTeam> getWinner() {
        List<? extends LockoutTeam> inPlay = getNonForfeitedTeams();
        Set<Map.Entry<LockoutTeam, Integer>> points = getPoints().entrySet().stream().filter(t -> inPlay.contains(t.getKey())).collect(Collectors.toSet());
        int openGoals = getRemainingGoals();
        if(points.size() == 1) {
            // Singleplayer; win if all goals are completed.
            return openGoals == 0 ? List.of(getNonForfeitedTeams().getFirst()) : List.of();
        }
        if(openGoals == 0) {
            // No more goals are available. Determine if there was a tie.
            int max = points.stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getValue).orElse(0);
            return points.stream().filter(p -> p.getValue() == max).map(Map.Entry::getKey).toList();
        }
        Map.Entry<LockoutTeam, Integer> max = null;
        Map.Entry<LockoutTeam, Integer> next = null;
        for(Map.Entry<LockoutTeam, Integer> team : points) {
            if(max == null || team.getValue() >= max.getValue()) {
                next = max;
                max = team;
            } else if(next == null || team.getValue() >= next.getValue()) {
                next = team;
            }
        }
        if(max == null) return List.of();
        if(max.getValue() - (next == null ? 0 : next.getValue()) > openGoals) {
            // No team can catch up to tie with the winner team.
            return List.of(max.getKey());
        }
        return List.of();
    }

    public int getRemainingGoals() {
        return (int) board.getGoals().stream().filter(goal -> !goal.hasAnyCompleted()).count();
    }

    private static String getWinnerTeamsString(List<? extends LockoutTeam> teams) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < teams.size(); i++) {
            if (i > 0) {
                if (i + 1 == teams.size()) {
                    sb.append(" and ");
                } else {
                    sb.append(", ");
                }
            }
            LockoutTeam team = teams.get(i);
            sb.append(team.getDisplayName());
        }
        return sb.toString();
    }

    public UpdateTimerPayload getUpdateTimerPacket() {
        return new UpdateTimerPayload(ticks);
    }

    public LockoutGamePayload getTeamsGoalsPacket() {
        return new LockoutGamePayload(
                teams.stream().collect(Collectors.toUnmodifiableList()),
                board.getGoals().stream().map(Goal::getBuildData).toList(),
                state
        );
    }

    public void forfeitTeam(LockoutTeam team) {
        // Mark as forfeited
        team.setForfeited(true);

        // Remove all players of this team from compass
        if (team instanceof ServerLockoutTeam teamServer) {
            // Process all members of the team
            for (java.util.UUID memberId : teamServer.getPlayerIds()) {
                LockoutServer.compassHandler.removePlayer(memberId);
            }
        }
        // Check if only 1 team remains and declare them winner if appropriate
        if (getNonForfeitedTeamsCount() == 1) {
            LockoutTeam winner = getNonForfeitedTeams().getFirst();
            LockoutServer.server.getPlayerList().broadcastSystemMessage(Component.literal(winner.getDisplayName() + " wins by default!"), false);

            // End game specifics
            setState(GameState.FINISHED);
            var payload = new EndLockoutPayload(new int[]{teams.indexOf(winner)}, System.currentTimeMillis());
            for (ServerPlayer serverPlayer : LockoutServer.server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(serverPlayer, payload);
            }
        }

        // Update clients with new team list (includes (Forfeited) text)
        for (ServerPlayer serverPlayer : LockoutServer.server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, getTeamsGoalsPacket());
        }
    }

    public List<? extends LockoutTeam> getNonForfeitedTeams() {
        return teams.stream().filter(t -> !t.isForfeited()).toList();
    }

    public int getNonForfeitedTeamsCount() {
        return (int) teams.stream().filter(t -> !t.isForfeited()).count();
    }

    public void save(Path path) throws IOException {
        if(!getState().isActive()) {
            Files.delete(path);
        } else {
            if(!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }

            JsonObject data = new JsonObject();
            JsonArray teamData = new JsonArray();
            teams.forEach(t -> teamData.add(t.serialize()));
            data.add("teams", teamData);
            data.add("board", board.serialize(this.teams));
            data.add("ticks", new JsonPrimitive(ticks));
            data.add("state", new JsonPrimitive(state.name()));

            String output = new GsonBuilder().setPrettyPrinting().create().toJson(data);
            Files.writeString(path, output);
        }
    }

    public static ServerLockoutGame load(Path path) throws IOException {
        if(!Files.exists(path)) return null;
        String content = Files.readString(path);
        JsonElement data = JsonParser.parseString(content);
        if(!data.isJsonObject()) throw new IOException("Invalid lockout save content.");
        JsonObject object = data.getAsJsonObject();

        JsonElement teamData = object.get("teams");
        if(teamData == null || !teamData.isJsonArray()) throw new IOException("Lockout save does not contain valid teams.");
        List<ServerLockoutTeam> teams = new ArrayList<>();
        for(JsonElement element : teamData.getAsJsonArray()) {
            teams.add(ServerLockoutTeam.deserialize(element, LockoutServer.server));
        }

        JsonElement boardData = object.get("board");
        if(boardData == null) throw new IOException("Lockout save does not contain a board.");
        ServerLockoutBoard board = ServerLockoutBoard.deserialize(boardData, teams);

        JsonElement ticks = object.get("ticks");
        if(ticks == null || !ticks.isJsonPrimitive() || !ticks.getAsJsonPrimitive().isNumber()) throw new IOException("Lockout save does not contain valid ticks.");

        JsonElement state = object.get("state");
        if(state == null || !state.isJsonPrimitive() || !state.getAsJsonPrimitive().isString()) throw new IOException("Lockout save does not contain valid state.");

        ServerLockoutGame lockout = new ServerLockoutGame(board, teams);
        lockout.setTicks(ticks.getAsLong());
        lockout.setState(GameState.valueOf(state.getAsString()));
        return lockout;
    }
}

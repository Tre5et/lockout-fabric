package me.marin.lockout;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.LockoutBoard;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.network.EndLockoutPayload;
import me.marin.lockout.network.LockoutGoalsTeamsPayload;
import me.marin.lockout.network.UpdateTimerPayload;
import me.marin.lockout.server.LockoutServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.scores.TeamColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Lockout {

    private static final Logger logger = LogManager.getLogger("Lockout");
    public static final Random random = new Random();
    public static final TeamColor[] COLOR_ORDERS = new TeamColor[]{TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW, TeamColor.GOLD, TeamColor.LIGHT_PURPLE, TeamColor.AQUA, TeamColor.DARK_PURPLE, TeamColor.DARK_AQUA, TeamColor.DARK_GREEN, TeamColor.WHITE, TeamColor.DARK_RED, TeamColor.GRAY, TeamColor.DARK_BLUE, TeamColor.DARK_GRAY, TeamColor.BLACK};

    public final Map<LockoutTeam, LinkedHashSet<EntityType<?>>> bredAnimalTypes = new HashMap<>();
    public final Map<LockoutTeam, LinkedHashSet<EntityType<?>>> killedHostileTypes = new HashMap<>();
    public final Map<LockoutTeam, LinkedHashSet<EntityType<?>>> killedRaidMobs = new HashMap<>();
    public final Map<LockoutTeam, Integer> killedUndeadMobs = new HashMap<>();
    public final Map<LockoutTeam, Integer> killedArthropods = new HashMap<>();
    public final Map<LockoutTeam, LinkedHashSet<Item>> foodTypesEaten = new HashMap<>();
    public final Map<LockoutTeam, LinkedHashSet<Identifier>> uniqueAdvancements = new HashMap<>();
    public final Map<LockoutTeam, LinkedHashSet<Identifier>> visitedBiomes = new HashMap<>();
    public final Map<LockoutTeam, Double> damageTaken = new HashMap<>();
    public final Map<LockoutTeam, Double> damageDealt = new HashMap<>();
    public final Map<LockoutTeam, Integer> deaths = new HashMap<>();
    public final Map<LockoutTeam, Integer> mobsKilled = new HashMap<>();

    // Tracks which teams have met the condition for each opponent goal (for 3+ team support)
    public final Map<Goal, Set<LockoutTeam>> opponentGoalProgress = new HashMap<>();

    public final Map<UUID, Integer> levels = new LinkedHashMap<>();
    public UUID mostLevelsPlayer;
    public int mostLevels;

    public final Map<UUID, Long> pumpkinWearTime = new HashMap<>();
    public final Map<UUID, Integer> distanceSprinted = new HashMap<>();
    public final Map<UUID, Integer> distanceBoated = new HashMap<>();
    public final Map<UUID, Set<Item>> uniqueCrafts = new HashMap<>();
    public final Map<UUID, Integer> playerKills = new HashMap<>();
    public final Map<UUID, Integer> playerAdvancements = new HashMap<>();
    public final Map<UUID, Integer> playerHopperCounts = new HashMap<>();
    public final Map<UUID, Integer> playerLeaflitterCounts = new HashMap<>();
    public final Map<UUID, Integer> playerDiamondBlockCounts = new HashMap<>();

    public UUID mostUniqueCraftsPlayer;
    public int mostUniqueCrafts;
    public UUID mostPlayerKillsPlayer;
    public int mostPlayerKills;
    public UUID mostAdvancementsPlayer;
    public int mostAdvancements;
    public UUID mostHoppersPlayer;
    public int mostHoppers;
    public UUID mostLeaflitterPlayer;
    public int mostLeaflitter;
    public UUID mostDiamondBlocksPlayer;
    public int mostDiamondBlocks;

    @Getter
    private final LockoutBoard board;
    @Getter
    private final List<? extends LockoutTeam> teams;
    private boolean hasStarted = false;
    @Getter
    @Setter
    private boolean isRunning = true;

    /**
     * Amount of *server* ticks the game has been running for.
     * Negative values mean that the game hasn't started yet (players are looking at the board).
     * This value is incremented by 1 every server tick.
     */
    @Setter
    @Getter
    private long ticks;

    public Lockout(LockoutBoard board, List<? extends LockoutTeam> teams) {
        this.board = board;
        this.teams = teams;
    }

    public static void log(String message) {
        logger.log(Level.INFO, message);
    }

    public static void error(Throwable t) {
        logger.error("Lockout error:\n", t);
    }

    public static boolean exists(Lockout lockout) {
        return lockout != null;
    }

    public static boolean isLockoutRunning(Lockout lockout) {
        return exists(lockout) && lockout.isRunning;
    }

    public String getModeName() {
        return teams.size() > 1 ? "Lockout" : "Blackout";
    }

    public boolean isSoloBlackout() {
        return teams.size() == 1 && teams.get(0).getPlayerNames().size() == 1;
    }

    public void tick() {
        ticks++;
    }

    public void evaluateWinnerAndEndGame(LockoutTeam team) {
        PlayerList playerManager = LockoutServer.server.getPlayerList();

        List<LockoutTeam> winners = new ArrayList<>();
        if (isWinner(team)) {
            playerManager.broadcastSystemMessage(Component.literal(team.getDisplayName() + " wins."), false);
            winners.add(team);
            setRunning(false);
        } else {
            if (getRemainingGoals() == 0 && getNonForfeitedTeamsCount() > 1) {
                int maxCompleted = getNonForfeitedTeams().stream().max(Comparator.comparingInt(LockoutTeam::getPoints)).get().getPoints();
                List<? extends LockoutTeam> winnerTeams = getNonForfeitedTeams().stream().filter(t -> t.getPoints() == maxCompleted).toList();
                winners.addAll(winnerTeams);
                playerManager.broadcastSystemMessage(Component.literal("It's a tie! " + getWinnerTeamsString(winnerTeams) + " win."), false);
                setRunning(false);
            }
        }

        if (!this.isRunning) {
            var payload = new EndLockoutPayload(winners.stream().mapToInt(winner -> teams.indexOf(winner)).toArray(), System.currentTimeMillis());
            for (ServerPlayer serverPlayer : LockoutServer.server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(serverPlayer, payload);
            }
        }
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void setStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
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

    public LockoutTeam getPlayerTeam(UUID playerId) {
        for (LockoutTeam team : teams) {
            if (team.containsPlayer(playerId)) {
                return team;
            }
        }
        return null;
    }

    public LockoutTeam getOpponentTeam(UUID playerId) {
        for (LockoutTeam team : teams) {
            if (!team.isForfeited() && !team.containsPlayer(playerId)) {
                return team;
            }
        }
        return null;
    }
    public LockoutTeam getOpponentTeam(LockoutTeam team) {
        for (LockoutTeam t : teams) {
            if (!t.isForfeited() && !Objects.equals(t, team)) {
                return t;
            }
        }
        return null;
    }

    public boolean isWinner(LockoutTeam team) {
        if (team.isForfeited()) return false;
        
        if (getNonForfeitedTeamsCount() == 1) {
            return getRemainingGoals() == 0;
        }
        for (LockoutTeam t : teams) {
            if (t.isForfeited() || team == t) continue;
            if (t.getPoints() + getRemainingGoals() >= team.getPoints()) {
                return false;
            }
        }
        return true;
    }

    public int getRemainingGoals() {
        return (int) board.getGoals().stream().filter(goal -> !goal.isCompleted()).count();
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

    public LockoutGoalsTeamsPayload getTeamsGoalsPacket() {
        return new LockoutGoalsTeamsPayload(teams.stream().map(team -> (LockoutTeam) team).toList(),
                board.getGoals().stream().map(goal -> {
                    String option = "null";
                    if(goal.getBuildData().getB() != null) {
                        option = goal.getBuildData().getB();
                    }
                    return new Pair<>(new Pair<>(goal.getBuildData().getA(), option), teams.indexOf(goal.getCompletedTeam()));
                }).toList(),
                isRunning);
    }

    public void forfeitTeam(LockoutTeam team) {
        // Mark as forfeited
        team.setForfeited(true);

        // Remove all players of this team from compass
        if (team instanceof LockoutTeamServer teamServer) {
            // Process all members of the team
            for (java.util.UUID memberId : teamServer.getPlayerIds()) {
                LockoutServer.compassHandler.removePlayer(memberId);
            }
        }
        
        // Update opponent goal progress
        for (Goal<?> goal : opponentGoalProgress.keySet()) {
            Set<LockoutTeam> progress = opponentGoalProgress.get(goal);
            progress.remove(team);
        }

        // Re-evaluate multi-opponent goals
        for (Goal<?> goal : new ArrayList<>(opponentGoalProgress.keySet())) {
            if (goal.isCompleted()) continue;
            
            Set<LockoutTeam> teamsMetCondition = opponentGoalProgress.get(goal);
            int activeTeamsCount = getNonForfeitedTeamsCount();
            int count = teamsMetCondition.size();
/*
            // Same logic as completeMultiOpponentGoal
            if (count >= activeTeamsCount - 1 && activeTeamsCount > 1) {
                List<LockoutTeam> winningTeams = getNonForfeitedTeams().stream()
                        .filter(t -> !teamsMetCondition.contains(t))
                        .map(t -> (LockoutTeam) t)
                        .toList();

                if (winningTeams.isEmpty()) continue;

                LockoutTeam winnerTeam = winningTeams.get(0);
                goal.setCompleted(true, winnerTeam);
                winnerTeam.addPoint();

                String completionMessage = winnerTeam.getDisplayName() + " completed the goal! (Opponent forfeited)";

                 for (LockoutTeam lockoutTeam : teams) {
                    if (!(lockoutTeam instanceof LockoutTeamServer lockoutTeamServer)) continue;
                    if (winningTeams.contains(lockoutTeamServer)) {
                        lockoutTeamServer.sendMessage(ChatFormatting.GREEN + completionMessage);
                    } else {
                        lockoutTeamServer.sendMessage(ChatFormatting.RED + completionMessage);
                    }
                }
                for (ServerPlayer spectator : Utility.getSpectators(this, LockoutServer.server)) {
                    spectator.sendSystemMessage(Component.literal(completionMessage));
                }

                goal.complete(winnerTeam);
                sendGoalCompletedPacket(goal, winnerTeam, null, false);
                evaluateWinnerAndEndGame(winnerTeam);
            }*/
        }

        // Recalculate "Most X" goals
/*        for (Goal goal : board.getGoals()) {
            if (goal == null) continue;
            if (goal instanceof HaveMostXPLevelsGoal) recalculateXPGoal(goal);
            if (goal instanceof HaveMostUniqueCraftsGoal) recalculateUniqueCraftsGoal(goal);
            if (goal instanceof HaveMostPlayerKillsGoal) recalculatePlayerKillsGoal(goal);
            if (goal instanceof HaveMostAdvancementsGoal) recalculateAdvancementsGoal(goal);
            if (goal instanceof HaveMostHoppersGoal) recalculateHoppersGoal(goal);
            if (goal instanceof HaveMostLeaflitterGoal) recalculateLeaflitterGoal(goal);
            if (goal instanceof HaveMostDiamondBlocksGoal) recalculateDiamondBlocksGoal(goal);
        }*/

        // Check if only 1 team remains and declare them winner if appropriate
        if (getNonForfeitedTeamsCount() == 1) {
             LockoutTeam winner = getNonForfeitedTeams().get(0);
             LockoutServer.server.getPlayerList().broadcastSystemMessage(Component.literal(winner.getDisplayName() + " wins by default!"), false);
             
             // End game specifics
             setRunning(false);
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
        if(!hasStarted) {
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

            String output = new GsonBuilder().setPrettyPrinting().create().toJson(data);
            Files.writeString(path, output);
        }
    }

    public static Lockout load(Path path) throws IOException {
        if(!Files.exists(path)) return null;
        String content = Files.readString(path);
        JsonElement data = JsonParser.parseString(content);
        if(!data.isJsonObject()) throw new IOException("Invalid lockout save content.");
        JsonObject object = data.getAsJsonObject();

        JsonElement teamData = object.get("teams");
        if(teamData == null || !teamData.isJsonArray()) throw new IOException("Lockout save does not contain valid teams.");
        List<LockoutTeam> teams = new ArrayList<>();
        for(JsonElement element : teamData.getAsJsonArray()) {
            teams.add(LockoutTeam.deserialize(element));
        }

        JsonElement boardData = object.get("board");
        if(boardData == null) throw new IOException("Lockout save does not contain a board.");
        LockoutBoard board = LockoutBoard.deserialize(boardData, teams);

        JsonElement ticks = object.get("ticks");
        if(ticks == null || !ticks.isJsonPrimitive() || !ticks.getAsJsonPrimitive().isNumber()) throw new IOException("Lockout save does not contain valid ticks.");

        Lockout lockout = new Lockout(board, teams);
        lockout.setStarted(true);
        lockout.setTicks(ticks.getAsLong());
        return lockout;
    }
}

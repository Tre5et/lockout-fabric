package me.marin.lockout.server;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.marin.lockout.*;
import me.marin.lockout.game.GameState;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.generator.BoardGenerator;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.config.GoalPoolConfig;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.network.*;
import me.marin.lockout.server.game.ServerLockoutBoard;
import me.marin.lockout.server.game.ServerLockoutGame;
import me.marin.lockout.server.goal.ServerGoal;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.handlers.*;
import net.minecraft.advancements.AdvancementHolder;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import me.lucko.fabric.api.permissions.v0.Permissions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static me.marin.lockout.Constants.PLACEHOLDER_PERM_STRING;

public class LockoutServer {

    public static int forfeitCommand(CommandContext<CommandSourceStack> context) {
        if (!LockoutGame.isActive(lockout)) return 0;
        
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null || !lockout.isLockoutPlayer(player)) {
            context.getSource().sendFailure(Component.literal("You are not participating in the Lockout match."));
            return 0;
        }

        ServerLockoutTeam team = lockout.getPlayerTeam(player.getUUID());

        // Broadcast first
        server.getPlayerList().broadcastSystemMessage(Component.literal(team.getDisplayName() + " has forfeited the match."), false);

        // Process all members of the team
        for (UUID memberId : team.getPlayerIds()) {
            ServerPlayer teamMember = server.getPlayerList().getPlayer(memberId);
            if (teamMember != null) {
                teamMember.setGameMode(GameType.SPECTATOR);
            }
        }

        lockout.forfeitTeam(team);

        return 1;
    }

    public static final int LOCATE_SEARCH = 750;
    public static GoalRequirementContext CONTEXT = GoalRequirementContext.EMPTY;

    public static ServerLockoutGame lockout;
    public static MinecraftServer server;
    public static CompassItemHandler compassHandler;

    public static final Map<LockoutRunnable, Long> gameStartRunnables = new HashMap<>();

    private static ServerLockoutBoard CUSTOM_BOARD = null;

    private static boolean isInitialized = false;

    public static Map<UUID, Long> waitingForVersionPacketPlayersMap = new HashMap<>();

    public static void initializeServer() {
        lockout = null;
        compassHandler = null;
        gameStartRunnables.clear();
        waitingForVersionPacketPlayersMap.clear();

        LockoutConfig.load(); // reload config every time the server starts
        GoalPoolConfig.load(); // reload goal pool config every time the server starts

        if (isInitialized) return;
        isInitialized = true;

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(new AllowChatMessageEventHandler());

        ServerPlayerEvents.AFTER_RESPAWN.register(new AfterRespawnEventHandler());

        //ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(new AfterPlayerChangeWorldEventHandler());

        ServerPlayConnectionEvents.JOIN.register(new PlayerJoinEventHandler());

        ServerTickEvents.END_SERVER_TICK.register(new EndServerTickEventHandler());
        
        // Add timeout handler for version packet checking
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            long currentTime = System.currentTimeMillis();
            long timeoutMs = 5000; // 5 second timeout
            
            // Check for players who haven't responded within timeout
            waitingForVersionPacketPlayersMap.entrySet().removeIf(entry -> {
                UUID playerUuid = entry.getKey();
                long joinTime = entry.getValue();
                
                if (currentTime - joinTime > timeoutMs) {
                    // Timeout expired, kick player
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                    if (player != null) {
                        player.connection.disconnect(Component.nullToEmpty("Missing Lockout mod.\nServer is using Lockout v" + LockoutInitializer.MOD_VERSION.getFriendlyString() + "."));
                    }
                    return true; // Remove from map
                }
                return false; // Keep in map
            });
        });

/*        ServerLivingEntityEvents.AFTER_DEATH.register(new AfterDeathEventHandler());

        UseBlockCallback.EVENT.register(new UseBlockEventHandler());*/

        ServerLifecycleEvents.SERVER_STARTED.register(new ServerStartedEventHandler());

        ServerLifecycleEvents.SERVER_STOPPING.register((_) -> isInitialized = false);

        ServerLifecycleEvents.AFTER_SAVE.register((server, _, _) -> {
            Path worldPath = server.getWorldPath(LevelResource.DATA);
            Path filePath = Path.of(worldPath.toAbsolutePath().toString(), "lockout", "game.json");
            try {
                if(lockout == null) {
                    Files.delete(filePath);
                } else {
                    lockout.save(filePath);
                }
                Lockout.log("Saved lockout state.");
            } catch (IOException e) {
                Lockout.log("Failed to save current lockout state: " + e.getMessage());
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> waitingForVersionPacketPlayersMap.remove(handler.getPlayer().getUUID()));

        ServerPlayNetworking.registerGlobalReceiver(LockoutVersionPayload.ID, (payload, context) -> {
            // Client has Lockout mod, compare versions, then kick or initialize
            ServerPlayer player = context.player();
            waitingForVersionPacketPlayersMap.remove(player.getUUID());

            String version = payload.version();
            if (!version.equals(LockoutInitializer.MOD_VERSION.getFriendlyString())) {
                player.connection.disconnect(Component.nullToEmpty("Wrong Lockout version: v" + version + ".\nServer is using Lockout v" + LockoutInitializer.MOD_VERSION.getFriendlyString() + "."));
                return;
            }

            sendAllAdvancements(player);

            if (!LockoutGame.isActive(lockout)) return;

            if (lockout.isLockoutPlayer(player.getUUID())) {
                player.setGameMode(GameType.SURVIVAL);
            } else {
                player.setGameMode(GameType.SPECTATOR);
                player.sendSystemMessage(Component.literal("You are spectating this match.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }

            ServerPlayNetworking.send(player, lockout.getTeamsGoalsPacket());
            ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());
            for(ServerGoal<?> goal : lockout.getBoard().getGoals()) {
                goal.getProgress().send(goal.getId(), List.of(player));
            }
            ServerPlayNetworking.send(player, StartLockoutPayload.INSTANCE);
            ServerLockoutTeam team = lockout.getPlayerTeam(player.getUUID());
            if (team != null) {
                team.sendStoredHints(player);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(CustomBoardPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();

            if (!server.isSingleplayer()) {
                if (!Permissions.check(player, PLACEHOLDER_PERM_STRING, LevelBasedPermissionSet.GAMEMASTER.level())) {
                    player.sendSystemMessage(Component.literal("You do not have the permission for this command!").withStyle(ChatFormatting.RED));
                    return;
                }
            }

            boolean clearBoard = payload.boardOrClear().isEmpty();
            if (clearBoard) {
                CUSTOM_BOARD = null;
                player.sendSystemMessage(Component.literal("Removed custom board."));
            } else {
                // validate board
                try {
                    List<ServerGoal<?>> goals = ServerGoal.constructAll(payload.boardOrClear().get());
                    CUSTOM_BOARD = new ServerLockoutBoard(goals);
                    player.sendSystemMessage(Component.literal("Set custom board."));
                } catch (IllegalGoalConstructionException e) {
                    player.sendSystemMessage(Component.literal(e.getMessage()));
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestHintPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();

            if (!LockoutGame.isActive(lockout)) return;
            if (!lockout.isLockoutPlayer(player.getUUID())) return;

            ServerGoal<?> goal = lockout.getBoard().getGoals().stream()
                    .filter(g -> g.getId().equals(payload.goalId()))
                    .findFirst()
                    .orElse(null);
            if (goal == null) return;

            int hintIndex = payload.hintIndex();
            if (hintIndex < 0 || hintIndex >= goal.getHints().size()) return;

            ServerHint<?> hint = goal.getHints().get(hintIndex);
            HintResultPayload resultPayload = hint.resolvePayload(payload.goalId(), hintIndex, server, player);

            if (resultPayload.data().isPresent()) {
                ServerLockoutTeam team = lockout.getPlayerTeam(player.getUUID());
                team.storeHintResult(payload.goalId(), hintIndex, resultPayload.data().get());
                for (UUID memberId : team.getPlayerIds()) {
                    ServerPlayer teamMember = server.getPlayerList().getPlayer(memberId);
                    if (teamMember != null) {
                        ServerPlayNetworking.send(teamMember, resultPayload);
                    }
                }
            } else {
                ServerPlayNetworking.send(player, resultPayload);
            }
        });
    }

    public static int lockoutCommandLogic(CommandContext<CommandSourceStack> context) {
        List<ServerLockoutTeam> teams = new ArrayList<>();

        int ret = parseArgumentsIntoTeams(teams, context, false);
        if (ret == 0) return 0;

        startLockout(teams);

        return 1;
    }

    public static int blackoutCommandLogic(CommandContext<CommandSourceStack> context) {
        List<ServerLockoutTeam> teams = new ArrayList<>();

        int ret = parseArgumentsIntoTeams(teams, context, true);
        if (ret == 0) return 0;

        startLockout(teams);

        return 1;
    }

    private static void startLockout(List<ServerLockoutTeam> teams) {
        // Clear old runnables
        gameStartRunnables.clear();

        PlayerList playerManager = server.getPlayerList();
        List<ServerPlayer> allServerPlayers = playerManager.getPlayers();
        List<UUID> allLockoutPlayers = teams.stream()
                .flatMap(team -> team.getPlayerIds().stream())
                .toList();

        for (ServerPlayer serverPlayer : allServerPlayers) {
            serverPlayer.getInventory().clearContent();
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
            serverPlayer.removeAllEffects();
            serverPlayer.getFoodData().setSaturation(5);
            serverPlayer.getFoodData().setFoodLevel(20);
            serverPlayer.getFoodData().exhaustionLevel = 0.0f;
            serverPlayer.setExperienceLevels(0);
            serverPlayer.setExperiencePoints(0);
            serverPlayer.setSharedFlagOnFire(false);

            // Clear all stats
            for (@SuppressWarnings("unchecked") StatType<Object> statType : new StatType[]{Stats.ITEM_CRAFTED, Stats.BLOCK_MINED, Stats.ITEM_USED, Stats.ITEM_BROKEN, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED, Stats.ENTITY_KILLED, Stats.ENTITY_KILLED_BY, Stats.CUSTOM}) {
                for (Identifier id : statType.getRegistry().keySet()) {
                    serverPlayer.resetStat(statType.get(statType.getRegistry().getValue(id)));
                }
            }
            serverPlayer.getStats().sendStats(serverPlayer);
            // Clear all advancements
            AdvancementCommands.Action.REVOKE.perform(serverPlayer, server.getAdvancements().getAllAdvancements(), false);

            if (allLockoutPlayers.contains(serverPlayer.getUUID())) {
                serverPlayer.setGameMode(GameType.ADVENTURE);
            } else {
                serverPlayer.setGameMode(GameType.SPECTATOR);
                serverPlayer.sendSystemMessage(Component.literal("You are spectating this match.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }

        ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);

        // Generate & set board
        ServerLockoutBoard lockoutBoard;
        if (CUSTOM_BOARD == null) {
            BoardGenerator boardGenerator = new BoardGenerator(GoalRegistry.INSTANCE.getRegisteredGoals(), new GoalRequirementContext(
                    CONTEXT.biomes(),
                    CONTEXT.structures(),
                    teams
            ));
            lockoutBoard = boardGenerator.generateBoard(LockoutConfig.getInstance().boardSize);
            
            // Check if board generation failed due to insufficient goals
            if (lockoutBoard == null) {
                String errorMessage = "Cannot generate board: Not enough goals enabled in goal-pool.yml. Please enable more goals or reduce board size.";
                for (UUID playerUuid : allLockoutPlayers) {
                    ServerPlayer player = playerManager.getPlayer(playerUuid);
                    if (player != null) {
                        player.sendOverlayMessage(Component.literal(errorMessage).withStyle(ChatFormatting.RED));
                    }
                }
                return; // Abort lockout start
            }
        } else {
            // Reset custom board (TODO: do this somewhere else)
            for (ServerGoal<?> goal : CUSTOM_BOARD.getGoals()) {
                goal.getProgress().resetAll();
            }
            lockoutBoard = CUSTOM_BOARD;
        }

        lockout = new ServerLockoutGame(lockoutBoard, teams);
        lockout.setTicks(-20L * LockoutConfig.getInstance().startTime); // see Lockout#ticks

        compassHandler = new CompassItemHandler(allLockoutPlayers, playerManager);

/*        List<Goal> tooltipGoals = new ArrayList<>(lockout.getBoard().getGoals()).stream().filter(g -> g.getTooltipInfo() != null).toList();
        for (Goal goal : tooltipGoals) {
            // Update teams tooltip
            for (LockoutTeam team : lockout.getTeams()) {
                ((LockoutTeamServer) team).sendTooltipUpdate(goal, false);
            }
            // Update spectator tooltip
            if (!allSpectatorPlayers.isEmpty()) {
                var payload = new UpdateTooltipPayload(goal.getId(), String.join("\n", goal.getSpectatorTooltip()));
                for (UUID spectator : allSpectatorPlayers) {
                    ServerPlayNetworking.send(playerManager.getPlayer(spectator), payload);
                }
            }
        }*/

        for (ServerPlayer player : allServerPlayers) {
            ServerPlayNetworking.send(player, lockout.getTeamsGoalsPacket());
            ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());

            if (!lockout.isSoloBlackout() && lockout.isLockoutPlayer(player.getUUID()) && LockoutConfig.getInstance().giveCompasses) {
                player.addItem(compassHandler.newCompass());
            }
        }

        world.clockManager().setTotalTicks(world.dimensionType().defaultClock().orElseThrow(), 0);

        // Unfreeze ticks when lockout/blackout game starts
        server.tickRateManager().setFrozen(true);

        for (int i = 3; i >= 0; i--) {
            if (i > 0) {
                final int secs = i;
                ((LockoutRunnable) () -> playerManager.broadcastSystemMessage(Component.literal("Starting in " + secs + "..."), false)).runTaskAfter(20L * (LockoutConfig.getInstance().startTime - i));
            } else {
                ((LockoutRunnable) LockoutServer::startLockoutRunning).runTaskAfter(20L * LockoutConfig.getInstance().startTime);
            }
        }
    }

    public static void startLockoutRunning() {
        if(lockout == null) return;
        server.tickRateManager().setFrozen(false);
        lockout.setState(GameState.RUNNING);

        List<UUID> playing = lockout.getTeams().stream().flatMap(t -> t.getPlayerIds().stream()).toList();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, StartLockoutPayload.INSTANCE);
            if (playing.contains(player.getUUID())) {
                player.setGameMode(GameType.SURVIVAL);

                // Update waypoint color to match team color with variation for team members
                LockoutTeam playerTeam = lockout.getPlayerTeam(player.getUUID());
                if (playerTeam != null) {
                    updatePlayerWaypointColor(player, playerTeam.getColor());
                }
            }
        }
        server.getPlayerList().broadcastSystemMessage(Component.literal(lockout.getModeName() + " has begun."), false);
    }

    /**
     * Updates a player's waypoint color to match their team color with slight variation for team members
     * @param player The player whose waypoint color should be updated
     * @param teamColor The team's color formatting
     */
    public static void updatePlayerWaypointColor(ServerPlayer player, TeamColor teamColor) {
        try {
            int colorValue = teamColor.rgb();
            
            String hexColor = String.format("%06X", colorValue | 0xFFFFFF);
            
            String command = String.format("waypoint modify %s color hex %s", player.getName().getString(), hexColor);
            
            // Create command source with appropriate permissions and silent execution
            CommandSourceStack commandSource = new CommandSourceStack(
                CommandSource.NULL, // Use dummy output to suppress chat messages
                player.position(),
                player.getRotationVector(),
                player.level(),
                LevelBasedPermissionSet.OWNER, // Permission level 4 (op level)
                player.getName().getString(),
                Component.empty(),
                server,
                player
            );
            
            // Parse and execute the command
            var parseResults = server.getCommands().getDispatcher().parse(command, commandSource);
            server.getCommands().performCommand(parseResults, command);
        } catch (Exception e) {
            // Silently ignore errors to avoid disrupting game start
            // Waypoint modification is not critical for game functionality
        }
    }
    
    /**
     * Creates a slight color variation for team members
     * @param baseColor The base team color
     * @param playerIndex The index of the player within their team
     * @return Modified color with slight variation
     */
    private static int createColorVariation(int baseColor, int playerIndex) {
        if (playerIndex == 0) {
            return baseColor; // First player gets the original team color
        }
        
        // Extract RGB components
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;
        
        // Create variation based on player index
        // Use different multipliers for each component to create noticeable but subtle differences
        double variation = 0.15; // 15% variation
        int variationAmount = (int) (variation * 255);
        
        // Apply different variations based on player index
        switch (playerIndex % 4) {
            case 1: // Slightly brighter
                r = Math.min(255, r + variationAmount);
                g = Math.min(255, g + variationAmount);
                b = Math.min(255, b + variationAmount);
                break;
            case 2: // Slightly darker
                r = Math.max(0, r - variationAmount);
                g = Math.max(0, g - variationAmount);
                b = Math.max(0, b - variationAmount);
                break;
            case 3: // Slightly more saturated (boost dominant color)
                int maxComponent = Math.max(Math.max(r, g), b);
                if (maxComponent == r) {
                    r = Math.min(255, r + variationAmount);
                } else if (maxComponent == g) {
                    g = Math.min(255, g + variationAmount);
                } else {
                    b = Math.min(255, b + variationAmount);
                }
                break;
        }
        
        return (r << 16) | (g << 8) | b;
    }

    private static int parseArgumentsIntoTeams(List<ServerLockoutTeam> teams, CommandContext<CommandSourceStack> context, boolean isBlackout) {
        String argument = null;

        PlayerList playerManager = server.getPlayerList();

        try {
            argument = context.getArgument("players", String.class);
            String[] players = argument.split(" +");
            if (players.length < 1) {
                context.getSource().sendFailure(Component.literal("Not enough players listed."));
                return 0;
            }

            if (isBlackout) {
                List<String> playerNames = new ArrayList<>();
                for (String player : players) {
                    if (playerManager.getPlayerByName(player) == null) {
                        context.getSource().sendFailure(Component.literal("Player " + player + " is invalid."));
                        return 0;
                    }
                    playerNames.add(playerManager.getPlayerByName(player).getName().getString());
                }
                teams.add(new ServerLockoutTeam(playerNames, Lockout.COLOR_ORDERS[0], server));

            } else {
                if (players.length > 16) {
                    context.getSource().sendFailure(Component.literal("Too many players listed."));
                    return 0;
                }

                for (int i = 0; i < players.length; i++) {
                    String player = players[i];
                    if (playerManager.getPlayerByName(player) == null) {
                        context.getSource().sendFailure(Component.literal("Player " + player + " is invalid."));
                        return 0;
                    }
                    teams.add(new ServerLockoutTeam(List.of(playerManager.getPlayerByName(player).getName().getString()), Lockout.COLOR_ORDERS[i], server));
                }
            }

        } catch (Exception ignored) {}

        if (argument == null) {
            try {
                ServerScoreboard scoreboard = server.getScoreboard();

                argument = context.getArgument("teams", String.class);
                String[] teamNames = argument.split(" +");
                if (teamNames.length < 1) {
                    context.getSource().sendFailure(Component.literal("Not enough teams listed."));
                    return 0;
                }
                if (isBlackout) {
                    if (teamNames.length > 1) {
                        context.getSource().sendFailure(Component.literal("Only one team can play Blackout."));
                        return 0;
                    }
                } else {
                    if (teamNames.length > 16) {
                        context.getSource().sendFailure(Component.literal("Too many teams listed."));
                        return 0;
                    }
                }

                List<PlayerTeam> scoreboardTeams = new ArrayList<>();
                for (String teamName : teamNames) {
                    PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                    if (team == null) {
                        context.getSource().sendFailure(Component.literal("Team " + teamName + " is invalid."));
                        return 0;
                    }
                    for (String player : team.getPlayers()) {
                        if (playerManager.getPlayerByName(player) == null) {
                            context.getSource().sendFailure(Component.literal("Player " + player + " on team " + teamName + " is invalid. Remove them from the team and try again."));
                            return 0;
                        }
                    }
                    scoreboardTeams.add(team);
                }
                for (PlayerTeam team : scoreboardTeams) {
                    if (team.getPlayers().isEmpty()) {
                        context.getSource().sendFailure(Component.literal("Team " + team.getName() + " doesn't have any players."));
                        return 0;
                    }
                    Optional<TeamColor> teamColor = team.getColor();
                    if (teamColor.isEmpty() || teamHasColor(teams, teamColor.get())) {
                        // Select an available color.
                        boolean found = false;
                        for (TeamColor colorOrder : Lockout.COLOR_ORDERS) {
                            if (!teamHasColor(teams, colorOrder)) {
                                found = true;
                                team.setColor(Optional.of(colorOrder));
                                break;
                            }
                        }
                        if (!found) {
                            context.getSource().sendFailure(Component.literal("Could not find assignable color for team " + team.getName() + ". Try recreating teams."));
                            return 0;
                        }
                    }
                    List<String> actualPlayerNames = new ArrayList<>();
                    for (String playerName : team.getPlayers()) {
                        actualPlayerNames.add(playerManager.getPlayerByName(playerName).getName().getString());
                    }
                    teams.add(new ServerLockoutTeam(new ArrayList<>(actualPlayerNames), team.getColor().orElse(TeamColor.BLACK), server));
                }
            } catch (Exception ignored) {}
        }

        if (argument == null) {
            context.getSource().sendFailure(Component.literal("Illegal argument."));
            return 0;
        }
        return 1;
    }

    private static boolean teamHasColor(List<ServerLockoutTeam> teams, TeamColor color) {
        for (LockoutTeam lockoutTeam : teams) {
            if (lockoutTeam.getColor() == color) {
                return true;
            }
        }
        return false;
    }

    public static int getChat(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This is a player-only command."));
            return 0;
        }

        ChatManager.Type curr = ChatManager.getChat(player);
        context.getSource().sendSystemMessage(Component.literal("You are currently chatting in " + curr.name() + "."));
        return 1;
    }

    public static int setChat(CommandContext<CommandSourceStack> context, ChatManager.Type type) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This is a player-only command."));
            return 0;
        }

        ChatManager.Type curr = ChatManager.getChat(player);
        if (curr == type) {
            player.sendSystemMessage(Component.nullToEmpty("You are already chatting in " + type.name() + "."));
        } else {
            player.sendSystemMessage(Component.nullToEmpty("You are now chatting in " + type.name() + "."));
            ChatManager.setChat(player, type);
        }
        return 1;
    }

    public static int getGoals(CommandContext<CommandSourceStack> context) {
        int goalNumber = GoalRegistry.INSTANCE.getRegisteredGoals().size();
        if(goalNumber == 0) {
            context.getSource().sendSystemMessage(Component.literal("There are no goal registered."));
            return 1;
        }
        String goals = GoalRegistry.INSTANCE.getRegisteredGoals().stream()
                .map(g -> "- " + g)
                .collect(Collectors.joining("\n"));
        context.getSource().sendSystemMessage(Component.literal("There are " + goalNumber + " goals registered:\n"+goals));
        return 1;
    }

    public static int grantGoal(CommandContext<CommandSourceStack> context) {
        try {
            if (!LockoutGame.isActive(lockout)) {
                context.getSource().sendFailure(Component.literal("There's no active lockout match."));
                return 0;
            }

            int idx = context.getArgument("goal", Integer.class);

            Collection<NameAndId> playerConfigs;
            try {
                playerConfigs = GameProfileArgument.getGameProfiles(context, "player");
            } catch (CommandSyntaxException e) {
                context.getSource().sendFailure(Component.literal("Invalid target."));
                return 0;
            }

            if (playerConfigs.size() != 1) {
                context.getSource().sendFailure(Component.literal("Invalid number of targets."));
                return 0;
            }
            NameAndId playerConfig = playerConfigs.stream().findFirst().get();
            if (!lockout.isLockoutPlayer(playerConfig.id())) {
                context.getSource().sendFailure(Component.literal("Player " + playerConfig.name() + " is not playing Lockout."));
                return 0;
            }

            if (idx > lockout.getBoard().getGoals().size()) {
                context.getSource().sendFailure(Component.literal("Goal number does not exist on the board."));
                return 0;
            }
            ServerGoal<?> goal = lockout.getBoard().getGoals().get(idx - 1);

            context.getSource().sendSystemMessage(Component.nullToEmpty("Granted a goal to " + playerConfig.name() + "."));
            goal.grant(lockout.getPlayerTeam(playerConfig.id()), lockout);
            return 1;
        } catch (RuntimeException e) {
            Lockout.error(e);
            return 0;
        }
    }

    public static int revokeGoal(CommandContext<CommandSourceStack> context) {
        try {
            if (!LockoutGame.isActive(lockout)) {
                context.getSource().sendFailure(Component.literal("There's no active lockout match."));
                return 0;
            }

            int idx = context.getArgument("goal", Integer.class);

            Collection<NameAndId> playerConfigs;
            try {
                playerConfigs = GameProfileArgument.getGameProfiles(context, "player");
            } catch (CommandSyntaxException e) {
                context.getSource().sendFailure(Component.literal("Invalid target."));
                return 0;
            }

            if (playerConfigs.size() != 1) {
                context.getSource().sendFailure(Component.literal("Invalid number of targets."));
                return 0;
            }
            NameAndId playerConfig = playerConfigs.stream().findFirst().get();
            if (!lockout.isLockoutPlayer(playerConfig.id())) {
                context.getSource().sendFailure(Component.literal("Player " + playerConfig.name() + " is not playing Lockout."));
                return 0;
            }

            if (idx > lockout.getBoard().getGoals().size()) {
                context.getSource().sendFailure(Component.literal("Goal number does not exist on the board."));
                return 0;
            }
            ServerGoal<?> goal = lockout.getBoard().getGoals().get(idx - 1);

            context.getSource().sendSystemMessage(Component.nullToEmpty("Revoked a goal from " + playerConfig.name() + "."));
            goal.revoke(lockout.getPlayerTeam(playerConfig.id()), lockout);
            return 1;
        } catch (RuntimeException e) {
            Lockout.error(e);
            return 0;
        }
    }

    public static int getStartTime(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("The current start time is " + LockoutConfig.getInstance().startTime + "s."));
        return 1;
    }

    public static int setStartTime(CommandContext<CommandSourceStack> context) {
        int seconds = context.getArgument("seconds", Integer.class);

        LockoutConfig.getInstance().startTime = seconds;
        LockoutConfig.save();

        context.getSource().sendSystemMessage(Component.nullToEmpty("Updated start time to " + seconds + "s."));
        return 1;
    }

    public static int clearCustomBoard(CommandContext<CommandSourceStack> context) {
        if(CUSTOM_BOARD == null) {
            context.getSource().sendSystemMessage(Component.literal("There is no custom board registered."));
            return 1;
        }
        CUSTOM_BOARD = null;
        context.getSource().sendSystemMessage(Component.literal("Cleared custom board."));
        return 1;
    }

    public static int getBoardSize(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("The current board size is " + LockoutConfig.getInstance().boardSize + "x" + LockoutConfig.getInstance().boardSize + "."));
        return 1;
    }

    public static int setBoardSize(CommandContext<CommandSourceStack> context) {
        int size = context.getArgument("size", Integer.class);

        LockoutConfig.getInstance().boardSize = size;
        LockoutConfig.save();

        context.getSource().sendSystemMessage(Component.nullToEmpty("Updated board size to " + size + "x" + size + "."));
        return 1;
    }

    private static void sendAllAdvancements(ServerPlayer player) {
        if (server == null) return;

        List<AdvancementHolder> advancements = server.getAdvancements().getAllAdvancements().stream()
                .sorted(Comparator.comparing(holder -> holder.id().toString()))
                .toList();
        ServerPlayNetworking.send(player, new AllAdvancementsPayload(advancements));
    }

    public static int getGiveCompasses(CommandContext<CommandSourceStack> context) {
        String message = LockoutConfig.getInstance().giveCompasses
                ? "Compasses will be given to players."
                : "Compasses will not be given to players.";
        context.getSource().sendSystemMessage(Component.literal(message));
        return 1;
    }

    public static int setGiveCompasses(CommandContext<CommandSourceStack> context) {
        boolean giveCompasses = context.getArgument("give", Boolean.class);
        LockoutConfig.getInstance().giveCompasses = giveCompasses;
        LockoutConfig.save();

        String message = giveCompasses
                ? "Compasses will now be given to players."
                : "Compasses will no longer be given to players.";
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    public static int reloadGoals(CommandContext<CommandSourceStack> context) {
        try {
            GoalPoolConfig.load();
            context.getSource().sendSuccess(() -> Component.literal("Goal pool configuration reloaded successfully."), true);
            return 1;
        } catch (Exception e) {
            Lockout.error(e);
            context.getSource().sendFailure(Component.literal("Failed to reload goal pool configuration. Check the server logs for details."));
            return 0;
        }
    }

    public static int getNearbyStructures(CommandContext<CommandSourceStack> context) {
        List<String> found = new ArrayList<>();
        for (Map.Entry<ResourceKey<Structure>, LocateData> entry : CONTEXT.structures().entrySet()) {
            if (entry.getValue().wasLocated()) {
                found.add(entry.getKey().identifier().getPath());
            }
        }
        Collections.sort(found);
        server.getPlayerList().broadcastSystemMessage(Component.empty().append(Component.literal("Found Structures:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(Component.literal("\n" + String.join("\n", found))), false);
        return 1;
    }

    public static int getNearbyBiomes(CommandContext<CommandSourceStack> context) {
        List<String> found = new ArrayList<>();
        for (Map.Entry<ResourceKey<Biome>, LocateData> entry : CONTEXT.biomes().entrySet()) {
            if (entry.getValue().wasLocated()) {
                found.add(entry.getKey().identifier().getPath());
            }
        }
        Collections.sort(found);
        server.getPlayerList().broadcastSystemMessage(Component.empty().append(Component.literal("Found Biomes:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(Component.literal("\n" + String.join("\n", found))), false);
        return 1;
    }

}
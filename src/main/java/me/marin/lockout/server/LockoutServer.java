package me.marin.lockout.server;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.marin.lockout.*;
import me.marin.lockout.client.LockoutBoard;
import me.marin.lockout.generator.BoardGenerator;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.config.GoalPoolConfig;
import me.marin.lockout.network.CustomBoardPayload;
import me.marin.lockout.network.LockoutVersionPayload;
import me.marin.lockout.network.StartLockoutPayload;
import me.marin.lockout.network.UpdateTooltipPayload;
import me.marin.lockout.server.handlers.*;
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import me.lucko.fabric.api.permissions.v0.Permissions;

import java.util.*;

import static me.marin.lockout.Constants.PLACEHOLDER_PERM_STRING;

public class LockoutServer {

    public static int forfeitCommand(CommandContext<CommandSourceStack> context) {
        if (!Lockout.isLockoutRunning(lockout)) return 0;
        
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null || !lockout.isLockoutPlayer(player)) {
            context.getSource().sendFailure(Component.literal("You are not participating in the Lockout match."));
            return 0;
        }

        LockoutTeam team = lockout.getPlayerTeam(player.getUUID());
        LockoutTeamServer teamServer = (LockoutTeamServer) team;

        // Broadcast first
        server.getPlayerList().broadcastSystemMessage(Component.literal(team.getDisplayName() + " has forfeited the match."), false);

        // Process all members of the team
        for (UUID memberId : teamServer.getPlayerIds()) {
            ServerPlayer teamMember = server.getPlayerList().getPlayer(memberId);
            if (teamMember != null) {
                teamMember.setGameMode(GameType.SPECTATOR);
            }
        }

        lockout.forfeitTeam(team);

        return 1;
    }

    public static final int LOCATE_SEARCH = 750;
    public static final Map<ResourceKey<Biome>, LocateData> BIOME_LOCATE_DATA = new HashMap<>();
    public static final Map<ResourceKey<Structure>, LocateData> STRUCTURE_LOCATE_DATA = new HashMap<>();
    public static final List<DyeColor> AVAILABLE_DYE_COLORS = new ArrayList<>();

    private static int lockoutStartTime;
    private static int boardSize;

    public static Lockout lockout;
    public static MinecraftServer server;
    public static CompassItemHandler compassHandler;

    public static final Map<LockoutRunnable, Long> gameStartRunnables = new HashMap<>();

    private static LockoutBoard CUSTOM_BOARD = null;

    private static boolean isInitialized = false;

    public static Map<UUID, Long> waitingForVersionPacketPlayersMap = new HashMap<>();

    public static void initializeServer() {
        lockout = null;
        compassHandler = null;
        gameStartRunnables.clear();
        waitingForVersionPacketPlayersMap.clear();

        // Ideally, rejoining a world gets detected here, and this data doesn't get wiped
        BIOME_LOCATE_DATA.clear();
        STRUCTURE_LOCATE_DATA.clear();
        AVAILABLE_DYE_COLORS.clear();

        LockoutConfig.load(); // reload config every time the server starts
        GoalPoolConfig.load(); // reload goal pool config every time the server starts
        lockoutStartTime = LockoutConfig.getInstance().lockoutStartTime;
        boardSize = LockoutConfig.getInstance().boardSize;
        Lockout.log("Using default board size: " + boardSize);
        Lockout.log("Using default lockout start time: " + lockoutStartTime);

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

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            isInitialized = false;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, minecraftServer) -> {
            waitingForVersionPacketPlayersMap.remove(handler.getPlayer().getUUID());
        });

        ServerPlayNetworking.registerGlobalReceiver(LockoutVersionPayload.ID, (payload, context) -> {
            // Client has Lockout mod, compare versions, then kick or initialize
            ServerPlayer player = context.player();
            waitingForVersionPacketPlayersMap.remove(player.getUUID());

            String version = payload.version();
            if (!version.equals(LockoutInitializer.MOD_VERSION.getFriendlyString())) {
                player.connection.disconnect(Component.nullToEmpty("Wrong Lockout version: v" + version + ".\nServer is using Lockout v" + LockoutInitializer.MOD_VERSION.getFriendlyString() + "."));
                return;
            }

            if (!Lockout.isLockoutRunning(lockout)) return;

            if (lockout.isLockoutPlayer(player.getUUID())) {
                LockoutTeamServer team = (LockoutTeamServer) lockout.getPlayerTeam(player.getUUID());
                for (Goal goal : lockout.getBoard().getGoals()) {
                    if (goal.getTooltipInfo() != null) {
                        ServerPlayNetworking.send(player, new UpdateTooltipPayload(goal.getId(), String.join("\n", goal.getTooltip(team, player))));
                    }
                }
                player.setGameMode(GameType.SURVIVAL);
            } else {
                for (Goal goal : lockout.getBoard().getGoals()) {
                    if (goal.getTooltipInfo() != null) {
                        ServerPlayNetworking.send(player, new UpdateTooltipPayload(goal.getId(), String.join("\n", goal.getSpectatorTooltip())));
                    }
                }
                player.setGameMode(GameType.SPECTATOR);
                player.sendSystemMessage(Component.literal("You are spectating this match.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }

            ServerPlayNetworking.send(player, lockout.getTeamsGoalsPacket());
            ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());
            if (lockout.hasStarted()) {
                ServerPlayNetworking.send(player, StartLockoutPayload.INSTANCE);
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
                    List<Goal> goals = GoalRegistry.INSTANCE.constructGoals(payload.boardOrClear().get());
                    CUSTOM_BOARD = new LockoutBoard(goals);
                    player.sendSystemMessage(Component.literal("Set custom board."));
                } catch (IllegalGoalConstructionException e) {
                    player.sendSystemMessage(Component.literal(e.getMessage()));
                }
            }
        });
    }

    public static LocateData locateBiome(MinecraftServer server, ResourceKey<Biome> biome) {
        if (BIOME_LOCATE_DATA.containsKey(biome)) return BIOME_LOCATE_DATA.get(biome);

        var spawnPoint = server.overworld().getRespawnData();
        var currentPos = spawnPoint.pos();

        var pair = server.overworld().findClosestBiome3d(
                biomeRegistryEntry -> biomeRegistryEntry.is(biome),
                currentPos,
                LOCATE_SEARCH,
                32,
                64);

        LocateData data= new LocateData(false,0);
        if (pair != null) {
            int distance = Mth.floor(LocateCommand.dist(currentPos.getX(), currentPos.getZ(), pair.getFirst().getX(), pair.getFirst().getZ()));
            if (distance < LOCATE_SEARCH) {
                data = new LocateData(true, distance);
            }
        }
        BIOME_LOCATE_DATA.put(biome, data);

        return data;
    }

    public static LocateData locateStructure(MinecraftServer server, ResourceKey<Structure> structure) {
        if (STRUCTURE_LOCATE_DATA.containsKey(structure)) return STRUCTURE_LOCATE_DATA.get(structure);

        var spawnPoint = server.overworld().getRespawnData();
        var currentPos = spawnPoint.pos();

        Registry<Structure> registry = server.overworld().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> structureList = HolderSet.direct(registry.getOrThrow(structure));

        var pair = server.overworld().getChunkSource().getGenerator().findNearestMapStructure(
                server.overworld(),
                structureList,
                currentPos,
                LOCATE_SEARCH,
                false);

        LocateData data = new LocateData(false, 0);
        if (pair != null) {
            int distance = Mth.floor(LocateCommand.dist(currentPos.getX(), currentPos.getZ(), pair.getFirst().getX(), pair.getFirst().getZ()));
            if (distance < LOCATE_SEARCH) {
                data = new LocateData(true, distance);
            }
        }
        STRUCTURE_LOCATE_DATA.put(structure, data);

        return data;
    }

    public static int lockoutCommandLogic(CommandContext<CommandSourceStack> context) {
        List<LockoutTeamServer> teams = new ArrayList<>();

        int ret = parseArgumentsIntoTeams(teams, context, false);
        if (ret == 0) return 0;

        startLockout(teams);

        return 1;
    }

    public static int blackoutCommandLogic(CommandContext<CommandSourceStack> context) {
        List<LockoutTeamServer> teams = new ArrayList<>();

        int ret = parseArgumentsIntoTeams(teams, context, true);
        if (ret == 0) return 0;

        startLockout(teams);

        return 1;
    }

    private static void startLockout(List<LockoutTeamServer> teams) {
        // Clear old runnables
        gameStartRunnables.clear();

        PlayerList playerManager = server.getPlayerList();
        List<ServerPlayer> allServerPlayers = playerManager.getPlayers();
        List<UUID> allLockoutPlayers = teams.stream()
                .flatMap(team -> team.getPlayerIds().stream())
                .toList();
        List<UUID> allSpectatorPlayers = allServerPlayers.stream()
                .map(ServerPlayer::getUUID)
                .filter(uuid -> !allLockoutPlayers.contains(uuid))
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
        LockoutBoard lockoutBoard;
        if (CUSTOM_BOARD == null) {
            BoardGenerator boardGenerator = new BoardGenerator(GoalRegistry.INSTANCE.getRegisteredGoals(), teams, BIOME_LOCATE_DATA, STRUCTURE_LOCATE_DATA);
            lockoutBoard = boardGenerator.generateBoard(boardSize);
            
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
            for (Goal goal : CUSTOM_BOARD.getGoals()) {
                goal.setCompleted(false, null);
            }
            lockoutBoard = CUSTOM_BOARD;
        }

        lockout = new Lockout(lockoutBoard, teams);
        lockout.setTicks(-20L * lockoutStartTime); // see Lockout#ticks

        compassHandler = new CompassItemHandler(allLockoutPlayers, playerManager);

        List<Goal> tooltipGoals = new ArrayList<>(lockout.getBoard().getGoals()).stream().filter(g -> g.getTooltipInfo() != null).toList();
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
        }

        for (ServerPlayer player : allServerPlayers) {
            ServerPlayNetworking.send(player, lockout.getTeamsGoalsPacket());
            ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());

            if (!lockout.isSoloBlackout() && lockout.isLockoutPlayer(player.getUUID()) && LockoutConfig.getInstance().giveCompasses) {
                player.addItem(compassHandler.newCompass());
            }
        }

        world.clockManager().setTotalTicks(world.dimensionType().defaultClock().orElseThrow(), 0);

        // Unfreeze ticks when lockout/blackout game starts
        var unfreezeCommand = "tick unfreeze";
        var unfreezeParseResults = server.getCommands().getDispatcher().parse(unfreezeCommand, server.createCommandSourceStack());
        server.getCommands().performCommand(unfreezeParseResults, unfreezeCommand);

        for (int i = 3; i >= 0; i--) {
            if (i > 0) {
                final int secs = i;
                ((LockoutRunnable) () -> {
                    playerManager.broadcastSystemMessage(Component.literal("Starting in " + secs + "..."), false);
                }).runTaskAfter(20L * (lockoutStartTime - i));
            } else {
                ((LockoutRunnable) () -> {
                    lockout.setStarted(true);

                    for (ServerPlayer player : allServerPlayers) {
                        if (player == null) continue;
                        ServerPlayNetworking.send(player, StartLockoutPayload.INSTANCE);
                        if (allLockoutPlayers.contains(player.getUUID())) {
                            player.setGameMode(GameType.SURVIVAL);
                            
                            // Update waypoint color to match team color with variation for team members
                            LockoutTeam playerTeam = lockout.getPlayerTeam(player.getUUID());
                            if (playerTeam != null) {
                                updatePlayerWaypointColor(player, playerTeam.getColor());
                            }
                        }
                    }
                    server.getPlayerList().broadcastSystemMessage(Component.literal(lockout.getModeName() + " has begun."), false);
                }).runTaskAfter(20L * lockoutStartTime);
            }
        }
    }

    /**
     * Updates a player's waypoint color to match their team color with slight variation for team members
     * @param player The player whose waypoint color should be updated
     * @param teamColor The team's color formatting
     */
    public static void updatePlayerWaypointColor(ServerPlayer player, TeamColor teamColor) {
        try {
            Integer colorValue = teamColor.textColor().getValue();
            if (colorValue == null) {
                return; // Skip if color has no RGB value
            }
            
            String hexColor = String.format("%06X", colorValue & 0xFFFFFF);
            
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

    private static int parseArgumentsIntoTeams(List<LockoutTeamServer> teams, CommandContext<CommandSourceStack> context, boolean isBlackout) {
        String argument = null;

        PlayerList playerManager = server.getPlayerList();

        try {
            argument = context.getArgument("player names", String.class);
            String[] players = argument.split(" +");
            if (isBlackout) {
                if (players.length == 0) {
                    context.getSource().sendFailure(Component.literal("Not enough players listed."));
                    return 0;
                }

                List<String> playerNames = new ArrayList<>();
                for (String player : players) {
                    if (playerManager.getPlayerByName(player) == null) {
                        context.getSource().sendFailure(Component.literal("Player " + player + " is invalid."));
                        return 0;
                    }
                    playerNames.add(playerManager.getPlayerByName(player).getName().getString());
                }
                teams.add(new LockoutTeamServer(playerNames, Lockout.COLOR_ORDERS[0], server));

            } else {
                if (players.length < 2) {
                    context.getSource().sendFailure(Component.literal("Not enough players listed. Make sure you separate player names with spaces."));
                    return 0;
                }
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
                    teams.add(new LockoutTeamServer(List.of(playerManager.getPlayerByName(player).getName().getString()), Lockout.COLOR_ORDERS[i], server));
                }
            }

        } catch (Exception ignored) {}

        if (argument == null) {
            try {
                ServerScoreboard scoreboard = server.getScoreboard();

                argument = context.getArgument(isBlackout ? "team name" : "team names", String.class);
                String[] teamNames = argument.split(" +");
                if (isBlackout) {
                    if (teamNames.length == 0) {
                        context.getSource().sendFailure(Component.literal("Not enough teams listed."));
                        return 0;
                    }
                    if (teamNames.length > 1) {
                        context.getSource().sendFailure(Component.literal("Only one team can play Blackout."));
                        return 0;
                    }
                } else {
                    if (teamNames.length < 2) {
                        context.getSource().sendFailure(Component.literal("Not enough teams listed. Make sure you separate team names with spaces."));
                        return 0;
                    }
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
                    teams.add(new LockoutTeamServer(new ArrayList<>(actualPlayerNames), team.getColor().orElse(TeamColor.BLACK), server));
                }
            } catch (Exception ignored) {}
        }

        if (argument == null) {
            context.getSource().sendFailure(Component.literal("Illegal argument."));
            return 0;
        }
        return 1;
    }

    private static boolean teamHasColor(List<LockoutTeamServer> teams, TeamColor color) {
        for (LockoutTeam lockoutTeam : teams) {
            if (lockoutTeam.getColor() == color) {
                return true;
            }
        }
        return false;
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

    public static int giveGoal(CommandContext<CommandSourceStack> context) {
        try {
            if (!Lockout.isLockoutRunning(lockout)) {
                context.getSource().sendFailure(Component.literal("There's no active lockout match."));
                return 0;
            }

            int idx = context.getArgument("goal number", Integer.class);

            Collection<NameAndId> playerConfigs;
            try {
                playerConfigs = GameProfileArgument.getGameProfiles(context, "player name");
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
            Goal goal = lockout.getBoard().getGoals().get(idx - 1);

            context.getSource().sendSystemMessage(Component.nullToEmpty("Gave " + playerConfig.name() + " goal \"" + goal.getName() + "\"."));
            lockout.updateGoalCompletion(goal, playerConfig.id());
            return 1;
        } catch (RuntimeException e) {
            Lockout.error(e);
            return 0;
        }
    }

    public static int setStartTime(CommandContext<CommandSourceStack> context) {
        int seconds = context.getArgument("seconds", Integer.class);

        lockoutStartTime = seconds;
        context.getSource().sendSystemMessage(Component.nullToEmpty("Updated start time to " + seconds + "s."));
        return 1;
    }

    public static int setBoardSize(CommandContext<CommandSourceStack> context) {
        int size = context.getArgument("board size", Integer.class);

        boardSize = size;
        context.getSource().sendSystemMessage(Component.nullToEmpty("Updated board size to " + size + "."));
        return 1;
    }

    public static int setGiveCompasses(CommandContext<CommandSourceStack> context) {
        boolean giveCompasses = context.getArgument("giveCompasses", Boolean.class);
        LockoutConfig.getInstance().giveCompasses = giveCompasses;
        LockoutConfig.save();

        String message = giveCompasses
                ? "Compasses will now be given to players"
                : "Compasses will no longer be given to players";
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    public static int reloadGoalPool(CommandContext<CommandSourceStack> context) {
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
        for (Map.Entry<ResourceKey<Structure>, LocateData> entry : STRUCTURE_LOCATE_DATA.entrySet()) {
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
        for (Map.Entry<ResourceKey<Biome>, LocateData> entry : BIOME_LOCATE_DATA.entrySet()) {
            if (entry.getValue().wasLocated()) {
                found.add(entry.getKey().identifier().getPath());
            }
        }
        Collections.sort(found);
        server.getPlayerList().broadcastSystemMessage(Component.empty().append(Component.literal("Found Biomes:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(Component.literal("\n" + String.join("\n", found))), false);
        return 1;
    }

}
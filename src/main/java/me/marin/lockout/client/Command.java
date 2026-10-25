package me.marin.lockout.client;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.marin.lockout.ChatManager;
import me.marin.lockout.game.GameMode;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.server.ServerConfig;
import me.marin.lockout.util.PlayerListSuggestionProvider;
import me.marin.lockout.util.TeamListSuggestionProvider;
import me.marin.lockout.util.LockoutTeamSuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

import java.util.function.Predicate;

import static me.marin.lockout.Constants.MAX_BOARD_SIZE;
import static me.marin.lockout.Constants.PLACEHOLDER_PERM_STRING;

public interface Command {
    Predicate<FabricClientCommandSource> OPERATOR = ccs -> {
            if (Minecraft.getInstance().isLocalServer()) {
                return true;
            }
            return Permissions.check(ccs, PLACEHOLDER_PERM_STRING, LevelBasedPermissionSet.GAMEMASTER.level());
    };

    static <T> ArgumentBuilder<FabricClientCommandSource, ?> getSet(String name, String argument, ArgumentType<T> type, com.mojang.brigadier.Command<FabricClientCommandSource> getFunction, com.mojang.brigadier.Command<FabricClientCommandSource> setFunction) {
        return ClientCommands.literal(name)
                .executes(getFunction)
                .then(ClientCommands.literal("get")
                        .executes(getFunction)
                ).then(ClientCommands.literal("set")
                        .then(ClientCommands.argument(argument, type)
                                .executes(setFunction)
                        )
                );
    }

    ArgumentBuilder<FabricClientCommandSource, ?> DISPLAY = ClientCommands.literal("display")
            .then(getSet("board-position", "position", BoardPositionArgumentType.newInstance(), LockoutClient::getBoardPosition, LockoutClient::setBoardPosition))
            .then(ClientCommands.literal("chat")
                    .executes(LockoutClient::getChatMode)
                    .then(ClientCommands.literal("team")
                            .executes(c -> LockoutClient.setChatMode(ChatManager.Type.TEAM, c))
                    ).then(ClientCommands.literal("local")
                            .executes(c -> LockoutClient.setChatMode(ChatManager.Type.LOCAL, c))
                    )
            );

    ArgumentBuilder<FabricClientCommandSource, ?> CONFIG = ClientCommands.literal("config")
            .requires(OPERATOR)
            .then(getSet("board-size", "size", IntegerArgumentType.integer(3,7), LockoutClient::getBoardSize, LockoutClient::setBoardSize))
            .then(getSet("start-time", "seconds", IntegerArgumentType.integer(5), LockoutClient::getStartTime, LockoutClient::setStartTime))
            .then(getSet("give-compasses", "give", BoolArgumentType.bool(), LockoutClient::getGiveCompasses, LockoutClient::setGiveCompasses))
            .then(ClientCommands.literal("hint-mode")
                    .executes(LockoutClient::getHintMode)
                    .then(ClientCommands.literal("get")
                            .executes(LockoutClient::getHintMode)
                    )
                    .then(ClientCommands.literal("set")
                            .then(ClientCommands.literal("text")
                                    .executes(c -> LockoutClient.setHintMode(ServerConfig.HintMode.TEXT, c))
                            ).then(ClientCommands.literal("compass")
                                    .executes(c -> LockoutClient.setHintMode(ServerConfig.HintMode.COMPASS, c))
                            ).then(ClientCommands.literal("map")
                                    .executes(c -> LockoutClient.setHintMode(ServerConfig.HintMode.MAP, c))
                            )
                    )
            ).then(ClientCommands.literal("difficulty")
                    .executes(LockoutClient::getDifficulty)
                    .then(ClientCommands.literal("get")
                            .executes(LockoutClient::getDifficulty)
                    )
                    .then(ClientCommands.literal("set")
                            .then(ClientCommands.literal("random")
                                    .executes(c -> LockoutClient.setDifficulty(GameDifficulty.RANDOM, c))
                            ).then(ClientCommands.literal("easy")
                                    .executes(c -> LockoutClient.setDifficulty(GameDifficulty.EASY, c))
                            ).then(ClientCommands.literal("normal")
                                    .executes(c -> LockoutClient.setDifficulty(GameDifficulty.NORMAL, c))
                            ).then(ClientCommands.literal("hard")
                                    .executes(c -> LockoutClient.setDifficulty(GameDifficulty.HARD, c))
                            )
                    )
            ).then(ClientCommands.literal("custom-board")
                    .then(ClientCommands.argument("board", CustomBoardFileArgumentType.newInstance())
                            .executes(LockoutClient::setCustomBoard)
                    ).then(ClientCommands.literal("clear")
                            .executes(LockoutClient::clearCustomBoard)
                    )
            ).then(ClientCommands.literal("reload-goals")
                    .executes(LockoutClient::reloadGoals)
            );

    ArgumentBuilder<FabricClientCommandSource, ?> BOARD_BUILDER = ClientCommands.literal("create-board")
            .executes(LockoutClient::openBoardBuilder)
            .then(ClientCommands.literal("new")
                    .executes(LockoutClient::openBoardBuilder)
            ).then(ClientCommands.literal("edit")
                    .then(ClientCommands.argument("board", CustomBoardFileArgumentType.newInstance())
                            .executes(LockoutClient::openBoardBuilder)
                    )
            );

    static ArgumentBuilder<FabricClientCommandSource,?> playerNames() {
        return ClientCommands.argument("players", StringArgumentType.greedyString()).suggests(new PlayerListSuggestionProvider());
    }

    static ArgumentBuilder<FabricClientCommandSource,?> teamNames() {
        return ClientCommands.argument("teams", StringArgumentType.greedyString()).suggests(new TeamListSuggestionProvider());
    }

    ArgumentBuilder<FabricClientCommandSource, ?> GAME = ClientCommands.literal("game")
            .then(ClientCommands.literal("start")
                    .requires(OPERATOR)
                    .then(ClientCommands.literal("lockout")
                            .then(playerNames().executes(c -> LockoutClient.start(GameMode.LOCKOUT, c)))
                            .then(ClientCommands.literal("players")
                                    .then(playerNames().executes(c -> LockoutClient.start(GameMode.LOCKOUT, c)))
                            ).then(ClientCommands.literal("teams")
                                    .then(teamNames().executes(c -> LockoutClient.start(GameMode.LOCKOUT, c)))
                            )
                    ).then(ClientCommands.literal("blackout")
                            .then(playerNames().executes(c -> LockoutClient.start(GameMode.BLACKOUT, c)))
                            .then(ClientCommands.literal("players")
                                    .then(playerNames().executes(c -> LockoutClient.start(GameMode.BLACKOUT, c)))
                            )
                    )
            ).then(ClientCommands.literal("pause")
                    .requires(OPERATOR)
                    .executes(LockoutClient::pause)
            ).then(ClientCommands.literal("unpause")
                    .requires(OPERATOR)
                    .executes(LockoutClient::unpause)
            ).then(ClientCommands.literal("forfeit")
                    .executes(LockoutClient::forfeit)
            ).then(ClientCommands.literal("goals")
                    .requires(OPERATOR)
                    .then(ClientCommands.literal("grant")
                            .then(ClientCommands.argument("goal", IntegerArgumentType.integer(1, MAX_BOARD_SIZE * MAX_BOARD_SIZE))
                                    .then(ClientCommands.argument("team", StringArgumentType.greedyString()).suggests(new LockoutTeamSuggestionProvider())
                                            .executes(LockoutClient::grantGoal)
                                    )
                            )
                    ).then(ClientCommands.literal("revoke")
                            .then(ClientCommands.argument("goal", IntegerArgumentType.integer(1, MAX_BOARD_SIZE * MAX_BOARD_SIZE))
                                    .then(ClientCommands.argument("team", StringArgumentType.greedyString()).suggests(new LockoutTeamSuggestionProvider())
                                            .executes(LockoutClient::revokeGoal)
                                    )
                            )
                    )
            );

    ArgumentBuilder<FabricClientCommandSource, ?> NEARBY = ClientCommands.literal("nearby")
            .requires(OPERATOR)
            .then(ClientCommands.literal("structures")
                    .executes(LockoutClient::getNearbyStructures)
            ).then(ClientCommands.literal("biomes")
                    .executes(LockoutClient::getNearbyBiomes)
            );

    CommandNode<FabricClientCommandSource> COMMAND = ClientCommands.literal("lockout")
            .then(DISPLAY)
            .then(CONFIG)
            .then(BOARD_BUILDER)
            .then(GAME)
            .then(NEARBY)
            .build();
}

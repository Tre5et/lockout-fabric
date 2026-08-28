package me.marin.lockout.server;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.marin.lockout.ChatManager;
import me.marin.lockout.util.PlayerSuggestionProvider;
import me.marin.lockout.util.TeamSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

import java.util.function.Predicate;

import static me.marin.lockout.Constants.MAX_BOARD_SIZE;
import static me.marin.lockout.Constants.PLACEHOLDER_PERM_STRING;

public interface Command {
    Predicate<CommandSourceStack> PERMISSIONS = (ssc) ->
            ssc.getServer() != null && (Permissions.check(ssc, PLACEHOLDER_PERM_STRING, LevelBasedPermissionSet.GAMEMASTER.level()) || ssc.getServer().isSingleplayer());

    static ArgumentBuilder<CommandSourceStack,?> playerNames() {
        return Commands.argument("players", StringArgumentType.greedyString()).suggests(new PlayerSuggestionProvider());
    }

    static ArgumentBuilder<CommandSourceStack,?> teamNames() {
        return Commands.argument("teams", StringArgumentType.greedyString()).suggests(new TeamSuggestionProvider());
    }

    static ArgumentBuilder<CommandSourceStack,?> start() {
        return Commands.literal("start")
                .requires(PERMISSIONS)
                .then(Commands.literal("players")
                        .then(playerNames().executes(LockoutServer::lockoutCommandLogic))
                ).then(Commands.literal("teams")
                        .then(teamNames().executes(LockoutServer::lockoutCommandLogic))
                ).then(Commands.literal("blackout")
                        .then(playerNames().executes(LockoutServer::blackoutCommandLogic))
                ).then(Commands.literal("time")
                        .executes(LockoutServer::getStartTime)
                        .then(Commands.literal("get")
                                .executes(LockoutServer::getStartTime)
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(5, 300))
                                        .executes(LockoutServer::setStartTime)
                                )
                        )
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> board() {
        return Commands.literal("board")
                .requires(PERMISSIONS)
                .then(Commands.literal("size")
                        .executes(LockoutServer::getBoardSize)
                        .then(Commands.literal("get")
                                .executes(LockoutServer::getBoardSize)
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("size", IntegerArgumentType.integer(3,7))
                                        .executes(LockoutServer::setBoardSize)
                                )
                        )
                ).then(Commands.literal("clear")
                        .executes(LockoutServer::clearCustomBoard)
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> goals() {
        return Commands.literal("goals")
                .requires(PERMISSIONS)
                .executes(LockoutServer::getGoals)
                .then(Commands.literal("reload")
                        .executes(LockoutServer::reloadGoals)
                ).then(Commands.literal("grant")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("goal", IntegerArgumentType.integer(1, MAX_BOARD_SIZE * MAX_BOARD_SIZE))
                                        .executes(LockoutServer::grantGoal)
                                )
                        )
                ).then(Commands.literal("revoke")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("goal", IntegerArgumentType.integer(1, MAX_BOARD_SIZE * MAX_BOARD_SIZE))
                                        .executes(LockoutServer::revokeGoal)
                                )
                        )
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> giveCompasses() {
        return Commands.literal("give-compasses")
                .requires(PERMISSIONS)
                .executes(LockoutServer::getGiveCompasses)
                .then(Commands.literal("get")
                        .executes(LockoutServer::getGiveCompasses)
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("give", BoolArgumentType.bool())
                                .executes(LockoutServer::setGiveCompasses)
                        )
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> nearby() {
        return Commands.literal("nearby")
                .requires(PERMISSIONS)
                .then(Commands.literal("structures")
                        .executes(LockoutServer::getNearbyStructures)
                ).then(Commands.literal("biomes")
                        .executes(LockoutServer::getNearbyBiomes)
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> chat() {
        return Commands.literal("chat")
                .executes(LockoutServer::getChat)
                .then(Commands.literal("team")
                        .executes(c -> LockoutServer.setChat(c, ChatManager.Type.TEAM))
                ).then(Commands.literal("local")
                        .executes(c -> LockoutServer.setChat(c, ChatManager.Type.LOCAL))
                );
    }

    static ArgumentBuilder<CommandSourceStack,?> forfeit() {
        return Commands.literal("forfeit")
                .executes(LockoutServer::forfeitCommand);
    }

    CommandNode<CommandSourceStack> SERVER_COMMAND = Commands.literal("lockout")
            .then(start())
            .then(board())
            .then(goals())
            .then(giveCompasses())
            .then(nearby())
            .then(chat())
            .then(forfeit())
            .build();
}

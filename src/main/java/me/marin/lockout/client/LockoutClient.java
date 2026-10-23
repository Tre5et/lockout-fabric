package me.marin.lockout.client;

import com.mojang.brigadier.context.CommandContext;
import lombok.Getter;
import me.marin.lockout.*;
import me.marin.lockout.client.game.ClientLockoutBoard;
import me.marin.lockout.client.game.ClientLockoutGame;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.gui.*;
import me.marin.lockout.game.GameMode;
import me.marin.lockout.game.GameState;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.network.*;
import me.marin.lockout.server.ServerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import java.io.IOException;
import java.util.*;

import static me.marin.lockout.Constants.*;

import com.mojang.blaze3d.platform.InputConstants;

public class LockoutClient implements ClientModInitializer {

    public static ClientLockoutGame lockout;
    public static LockoutTeam playerTeam = null;
    private static KeyMapping openBoardKey;
    @Getter
    private static List<KeyMapping> hintKeys;
    public static int CURRENT_TICK = 0;
    public static final Map<Identifier, Advancement> allAdvancements = new HashMap<>();

    public static KeyMapping getBoardKeybinding() {
        return openBoardKey;
    }

    public static Map<Identifier, Advancement> getAllAdvancements() {
        return Map.copyOf(allAdvancements);
    }

    public static final MenuType<BoardScreenHandler> BOARD_SCREEN_HANDLER;
    public static final KeyMapping.Category LOCKOUT_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "keybinds"));

    static {
        BOARD_SCREEN_HANDLER = new MenuType<>(BoardScreenHandler::new, FeatureFlags.VANILLA_SET);
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.load();

        Registry.register(BuiltInRegistries.MENU, Constants.BOARD_SCREEN_ID, BOARD_SCREEN_HANDLER);

        ClientPlayNetworking.registerGlobalReceiver(AllAdvancementsPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    allAdvancements.clear();
                    for(AdvancementHolder holder : payload.advancements()) {
                        allAdvancements.put(holder.id(), holder.value());
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(LockoutGamePayload.ID, (payload, context) -> {
            // Ensure goals are registered at packet handling time, when itemStack stacks are available client-side.
            List<LockoutTeam> teams = payload.teams();

            LockoutClient.playerTeam = teams.stream()
                    .filter(t -> t.getPlayerIds().stream().anyMatch(id -> id.equals(Minecraft.getInstance().player.getUUID())))
                    .findAny().orElse(null);

            boolean previouslyStarted = lockout != null && lockout.getState().isShouldSave();
            long previousTicks = lockout != null ? lockout.getTicks() : 0;

            Minecraft client = context.client();
            try {
                List<ClientGoal> goals = ClientGoal.constructAll(payload.goals());
                lockout = new ClientLockoutGame(new ClientLockoutBoard(goals), teams);
            } catch (IllegalGoalConstructionException e) {
                if(client.player != null) client.player.sendSystemMessage(Component.literal(e.getMessage()));
                Lockout.log(e.getMessage());
                return;
            }

            lockout.setState(payload.state());
            lockout.setTicks(previousTicks);

            client.execute(() -> {
                if (client.player != null && !previouslyStarted) {
                    client.gui.setScreen(new BoardScreen(BOARD_SCREEN_HANDLER.create(0, client.player.getInventory()), client.player.getInventory(), Component.empty()));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(StartLockoutPayload.ID, (_, context) -> {
            lockout.setState(GameState.RUNNING);
            context.client().execute(() -> {
                if (Minecraft.getInstance().gui.screen() != null) {
                    Minecraft.getInstance().gui.screen().onClose();
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(UpdateTimerPayload.ID, (payload, _) -> lockout.setTicks(payload.ticks()));
        ClientPlayNetworking.registerGlobalReceiver(HintResultPayload.ID, (payload, _) -> {
            Optional<ClientGoal> goal = lockout.getBoard().getGoals().stream()
                    .filter(g -> g.getId().equals(payload.goalId()))
                    .findAny();
            if(goal.isEmpty() || goal.get().getHints().size() <= payload.hintIndex()) return;
            ClientHint<?> hint = goal.get().getHints().get(payload.hintIndex());
            try {
                hint.updatePayload(payload);
            } catch (IllegalArgumentException e) {
                Lockout.error(e);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(GoalProgressPayload.ID, (payload, context) -> {
            if (lockout == null) return;
            lockout.getBoard().getGoals().stream()
                    .filter(g -> g.getId().equals(payload.goalId()))
                    .findFirst()
                    .ifPresent(g -> {
                        g.updateProgress(payload.progress());
                        if(payload.newCompletion().isPresent()) {
                            g.announceCompletion(payload.newCompletion().get(), lockout, context.client());
                        }
                    });
        });
        ClientPlayNetworking.registerGlobalReceiver(EndLockoutPayload.ID, (payload, context) -> {
            lockout.setState(GameState.FINISHED);
            Minecraft client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    boolean didIWin = false;
                    for (int winner : payload.winners()) {
                        LockoutTeam team = lockout.getTeams().get(winner);

                        if (team.getPlayerNames().contains(client.player.getName().getString())) {
                            didIWin = true;
                            break;
                        }
                    }
                    client.gui.hud.resetTitleTimes();
                    if (didIWin) {
                        client.player.playSound(SoundEvents.PILLAGER_CELEBRATE, 2f, 1f);
                        if(payload.winners().length > 1) {
                            client.gui.hud.setTitle(Component.literal("Its a Tie!").withColor(TextColor.AQUA));
                        } else {
                            client.gui.hud.setTitle(Component.literal("You Win!").withColor(TextColor.GREEN));
                        }
                    } else {
                        client.player.playSound(SoundEvents.WARDEN_DEATH, 2f, 1f);
                        client.gui.hud.setTitle(Component.literal("You Lost!").withColor(TextColor.DARK_RED));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerConfigPayload.ID, (payload, context) -> {
            ClientConfig.getInstance().setServerConfig(payload.config());
        });

        ArgumentTypeRegistry.registerArgumentType(Constants.BOARD_FILE_ARGUMENT_TYPE, CustomBoardFileArgumentType.class, SingletonArgumentInfo.contextFree(CustomBoardFileArgumentType::newInstance));
        ArgumentTypeRegistry.registerArgumentType(Constants.BOARD_POSITION_ARGUMENT_TYPE, BoardPositionArgumentType.class, SingletonArgumentInfo.contextFree(BoardPositionArgumentType::newInstance));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.getRoot().addChild(Command.COMMAND));

        ClientPlayNetworking.registerGlobalReceiver(LockoutVersionPayload.ID, (payload, _) -> {
            // Compare Lockout versions, disconnect if invalid.
            String version = payload.version();
            if (!version.equals(LockoutInitializer.MOD_VERSION.getFriendlyString())) {
                Minecraft.getInstance().player.connection.getConnection().disconnect(Component.nullToEmpty("Wrong Lockout version: v" + LockoutInitializer.MOD_VERSION.getFriendlyString() + ".\nServer is using Lockout v" + version + "."));
                return;
            }

            // Respond with version, it will be compared on server as well
            ClientPlayNetworking.send(new LockoutVersionPayload(LockoutInitializer.MOD_VERSION.getFriendlyString()));
        });
        
        openBoardKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lockout.open_board", // The translation key of the keybinding's name
                InputConstants.Type.KEYBOARD, // The entity of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                InputConstants.KEY_B, // The keycode of the key
                LOCKOUT_CATEGORY // The translation key of the keybinding's category.
        ));

        List<Integer> defaultHintKeys = List.of(InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3, InputConstants.KEY_4, InputConstants.KEY_5, InputConstants.KEY_6, InputConstants.KEY_7, InputConstants.KEY_8, InputConstants.KEY_9, InputConstants.KEY_0);
        List<KeyMapping> hintMappings = new ArrayList<>();
        for(int i = 0; i < defaultHintKeys.size(); i++) {
            hintMappings.add(KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.lockout.hint_" + i,
                    InputConstants.Type.KEYBOARD,
                    defaultHintKeys.get(i),
                    LOCKOUT_CATEGORY
            )));
        }
        hintKeys = hintMappings;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CURRENT_TICK++;

            if (lockout != null && lockout.getState() == GameState.PAUSED) {
                client.gui.hud.setTimes(0, 2, 0);
                client.gui.hud.setTitle(Component.literal("The game is paused."));
            }

            boolean wasPressed = false;
            while (openBoardKey.consumeClick()) {
                wasPressed = true;
            }
            if (wasPressed) {
                if (client.gui.screen() != null || client.player == null) {
                    return;
                }

                // If the game hasn't started, open board builder instead
                if (lockout == null) {
                    client.gui.setScreen(new BoardBuilderScreen());
                    return;
                }

                // Open GUI
                client.gui.setScreen(new BoardScreen(BOARD_SCREEN_HANDLER.create(0, client.player.getInventory()), client.player.getInventory(), Component.empty()));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register(((_, _) -> {
            lockout = null;
            allAdvancements.clear();
        }));

        MenuScreens.register(BOARD_SCREEN_HANDLER, BoardScreen::new);
    }

    public static int setCustomBoard(CommandContext<FabricClientCommandSource> context) {
        String boardName = context.getArgument("board", String.class);

        List<ClientGoal> goals;
        try {
            goals = BoardBuilderIO.INSTANCE.readBoard(boardName);
        } catch (IOException e) {
            context.getSource().sendError(Component.literal("Error while trying to read board."));
            Lockout.error(e);
            return 0;
        }

        int size = (int) Math.sqrt(goals.size());
        if (size * size != goals.size() || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
            context.getSource().sendError(Component.literal("Board doesn't have a valid number of goals!"));
            return 0;
        }

        Network.send(new CustomBoardPayload(Optional.of(goals.stream()
                .map(ClientGoal::getBuildData).toList())));
        return 1;
    }

    public static int clearCustomBoard(CommandContext<FabricClientCommandSource> context) {
        Network.send(new CustomBoardPayload(Optional.empty()));
        return 1;
    }

    public static int openBoardBuilder(CommandContext<FabricClientCommandSource> context) {
        try {
            String boardName = context.getArgument("board", String.class);
            List<ClientGoal> goals;
            try {
                goals = BoardBuilderIO.INSTANCE.readBoard(boardName);
            } catch (IOException e) {
                context.getSource().sendError(Component.literal("Error while trying to read board."));
                Lockout.error(e);
                return 0;
            }

            int size = (int) Math.sqrt(goals.size());
            if (size * size != goals.size() || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
                context.getSource().sendError(Component.literal("Board doesn't have a valid number of goals!"));
                return 0;
            }

            BoardBuilderData.INSTANCE.setBoard(boardName, goals);
        } catch (IllegalArgumentException ignored) {}

        Minecraft client = Minecraft.getInstance();
        client.schedule(() -> {
            if (client.player != null) {
                client.gui.setScreen(new BoardBuilderScreen());
            }
        });

        return 1;
    }

    public static int getBoardPosition(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("The current board position is " + ClientConfig.getInstance().getBoardPosition().name().toLowerCase() + "."));
        return 1;
    }

    public static int setBoardPosition(CommandContext<FabricClientCommandSource> context) {
        String position = context.getArgument("position", String.class);

        ClientConfig.BoardPosition boardPosition = ClientConfig.BoardPosition.match(position);
        if (boardPosition == null) {
            context.getSource().sendError(Component.literal("Invalid board position: " + position + "."));
            return 0;
        }
        ClientConfig.getInstance().setBoardPosition(boardPosition);

        context.getSource().sendFeedback(Component.literal("Updated board position." + (boardPosition == ClientConfig.BoardPosition.LEFT ? " Note: Opening debug hud (F3) will hide the board." : "")));

        return 1;
    }

    public static int getBoardSize(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("The current board size is " + ClientConfig.getInstance().getServerConfig().getBoardSize() + "x" + ClientConfig.getInstance().getServerConfig().getBoardSize() + "."));
        return 1;
    }

    public static int setBoardSize(CommandContext<FabricClientCommandSource> context) {
        int size = context.getArgument("size", Integer.class);
        Network.send(UpdateSettingsPayload.boardSize(size));
        return 1;
    }

    public static int getStartTime(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("The current start time is " + ClientConfig.getInstance().getServerConfig().getStartTime() + "s."));
        return 1;
    }

    public static int setStartTime(CommandContext<FabricClientCommandSource> context) {
        int size = context.getArgument("seconds", Integer.class);
        Network.send(UpdateSettingsPayload.startTime(size));
        return 1;
    }

    public static int getGiveCompasses(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("Compasses are currently " + (ClientConfig.getInstance().getServerConfig().isGiveCompasses() ? "" : "not ") + "given out."));
        return 1;
    }

    public static int setGiveCompasses(CommandContext<FabricClientCommandSource> context) {
        boolean size = context.getArgument("give", Boolean.class);
        Network.send(UpdateSettingsPayload.giveCompasses(size));
        return 1;
    }

    public static int getHintMode(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("The current hint mode is " + ClientConfig.getInstance().getServerConfig().getHintMode().name().toLowerCase(Locale.ROOT) + "."));
        return 1;
    }

    public static int setHintMode(ServerConfig.HintMode mode, CommandContext<FabricClientCommandSource> context) {
        Network.send(UpdateSettingsPayload.hintMode(mode));
        return 1;
    }

    public static int getDifficulty(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("The current difficulty is " + ClientConfig.getInstance().getServerConfig().getDifficulty().name().toLowerCase(Locale.ROOT) + "."));
        return 1;
    }

    public static int setDifficulty(GameDifficulty difficulty, CommandContext<FabricClientCommandSource> context) {
        Network.send(UpdateSettingsPayload.difficulty(difficulty));
        return 1;
    }

    public static int getChatMode(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.GET_CHAT_MODE));
        return 1;
    }

    public static int setChatMode(ChatManager.Type mode, CommandContext<FabricClientCommandSource> context) {
        Network.send(UpdateSettingsPayload.chatMode(mode));
        return 1;
    }

    public static int reloadGoals(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.RELOAD_GOALS));
        return 1;
    }

    public static int start(GameMode gameMode, CommandContext<FabricClientCommandSource> context) {
        String players = null;
        try {
            players = context.getArgument("players", String.class);
        } catch (Exception ignored) {}

        String teams = null;
        try {
            teams = context.getArgument("teams", String.class);
        } catch (Exception ignored) {}

        if(players == null && teams == null) {
            context.getSource().sendError(Component.literal("No players or teams specified."));
            return 0;
        }

        Network.send(RequestActionPayload.start(gameMode, players, teams));
        return 1;
    }

    public static int pause(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.PAUSE));
        return 1;
    }

    public static int unpause(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.UNPAUSE));
        return 1;
    }

    public static int forfeit(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.FORFEIT));
        return 1;
    }

    public static int grantGoal(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.goal(RequestActionPayload.Action.GRANT_GOAL, context.getArgument("goal", Integer.class), context.getArgument("team", String.class)));
        return 1;
    }

    public static int revokeGoal(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.goal(RequestActionPayload.Action.REVOKE_GOAL, context.getArgument("goal", Integer.class), context.getArgument("team", String.class)));
        return 1;
    }

    public static int getNearbyStructures(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.NEARBY_STRUCTURES));
        return 1;
    }

    public static int getNearbyBiomes(CommandContext<FabricClientCommandSource> context) {
        Network.send(RequestActionPayload.simple(RequestActionPayload.Action.NEARBY_BIOMES));
        return 1;
    }
}

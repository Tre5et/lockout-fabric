package me.marin.lockout.client;

import com.mojang.brigadier.context.CommandContext;
import lombok.Getter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.marin.lockout.*;
import me.marin.lockout.client.game.ClientLockoutBoard;
import me.marin.lockout.client.game.ClientLockoutGame;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.gui.*;
import me.marin.lockout.game.GameState;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.lwjgl.glfw.GLFW;

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

            boolean previouslyStarted = lockout != null && lockout.getState().isActive();
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
                    if (didIWin) {
                        client.player.playSound(SoundEvents.PILLAGER_CELEBRATE, 2f, 1f);
                    } else {
                        client.player.playSound(SoundEvents.WARDEN_DEATH, 2f, 1f);
                    }
                }
            });
        });

        ArgumentTypeRegistry.registerArgumentType(Constants.BOARD_FILE_ARGUMENT_TYPE, CustomBoardFileArgumentType.class, SingletonArgumentInfo.contextFree(CustomBoardFileArgumentType::newInstance));
        ArgumentTypeRegistry.registerArgumentType(Constants.BOARD_POSITION_ARGUMENT_TYPE, BoardPositionArgumentType.class, SingletonArgumentInfo.contextFree(BoardPositionArgumentType::newInstance));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.getRoot().addChild(ClientCommands.literal("lockoutc")
                .then(ClientCommands.literal("board")
                        .then(ClientCommands.literal("set")
                                .requires(ccs -> {
                                    if (Minecraft.getInstance().isLocalServer()) {
                                        return true;
                                    }
                                    return Permissions.check(ccs, PLACEHOLDER_PERM_STRING, LevelBasedPermissionSet.GAMEMASTER.level());
                                }).then(ClientCommands.argument("name", CustomBoardFileArgumentType.newInstance())
                                        .executes(LockoutClient::setCustomBoard)
                                )
                        )
                        .then(ClientCommands.literal("builder")
                                .executes(LockoutClient::openBoardBuilder)
                                .then(ClientCommands.argument("board", CustomBoardFileArgumentType.newInstance())
                                        .executes(LockoutClient::openBoardBuilder)
                                )
                        )
                        .then(ClientCommands.literal("position")
                                .executes(LockoutClient::getBoardPosition)
                                .then(ClientCommands.literal("set")
                                        .then(ClientCommands.argument("position", BoardPositionArgumentType.newInstance())
                                                .executes(LockoutClient::setBoardPosition)
                                        )
                                )
                        )
                ).build()
        ));

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
                InputConstants.Type.KEYSYM, // The entity of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_B, // The keycode of the key
                LOCKOUT_CATEGORY // The translation key of the keybinding's category.
        ));

        List<Integer> defaultHintKeys = List.of(GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_0);
        List<KeyMapping> hintMappings = new ArrayList<>();
        for(int i = 0; i < defaultHintKeys.size(); i++) {
            hintMappings.add(KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.lockout.hint_" + i,
                    InputConstants.Type.KEYSYM,
                    defaultHintKeys.get(i),
                    LOCKOUT_CATEGORY
            )));
        }
        hintKeys = hintMappings;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CURRENT_TICK++;

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
        String boardName = context.getArgument("name", String.class);

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

        ClientPlayNetworking.send(new CustomBoardPayload(Optional.of(goals.stream()
                .map(ClientGoal::getBuildData).toList())));
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
        context.getSource().sendFeedback(Component.literal("The current board position is " + LockoutConfig.getInstance().boardPosition.name().toLowerCase() + "."));
        return 1;
    }

    public static int setBoardPosition(CommandContext<FabricClientCommandSource> context) {
        String position = context.getArgument("position", String.class);

        LockoutConfig.BoardPosition boardPosition = LockoutConfig.BoardPosition.match(position);
        if (boardPosition == null) {
            context.getSource().sendError(Component.literal("Invalid board position: " + position + "."));
            return 0;
        }
        LockoutConfig.getInstance().boardPosition = boardPosition;
        LockoutConfig.save();

        context.getSource().sendFeedback(Component.literal("Updated board position." + (boardPosition == LockoutConfig.BoardPosition.LEFT ? " Note: Opening debug hud (F3) will hide the board." : "")));

        return 1;
    }

}

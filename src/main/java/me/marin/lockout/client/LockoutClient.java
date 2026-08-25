package me.marin.lockout.client;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.marin.lockout.*;
import me.marin.lockout.client.gui.*;
import me.marin.lockout.json.JSONBoard;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import me.marin.lockout.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.ChatFormatting;
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
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.util.*;

import static me.marin.lockout.Constants.*;

import com.mojang.blaze3d.platform.InputConstants;

public class LockoutClient implements ClientModInitializer {

    public static Lockout lockout;
    public static LockoutTeam playerTeam = null;
    private static KeyMapping keyBinding;
    public static int CURRENT_TICK = 0;
    public static final Map<Identifier, Advancement> allAdvancements = new HashMap<>();
    /** goalId -> hintIndex -> result message; persists until disconnect */
    public static final Map<String, Map<Integer, String>> goalHintResults = new HashMap<>();
    /** goalId -> hintIndex -> error message; cleared when the BoardScreen closes */
    public static final Map<String, Map<Integer, String>> goalHintErrors = new HashMap<>();

    public static KeyMapping getBoardKeybinding() {
        return keyBinding;
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
        ClientPlayNetworking.registerGlobalReceiver(LockoutGoalsTeamsPayload.ID, (payload, context) -> {
            // Ensure goals are registered at packet handling time, when item stacks are available client-side.
            List<LockoutTeam> teams = payload.teams();

            LockoutClient.playerTeam = teams.stream()
                    .filter(t -> t.getPlayerIds().stream().anyMatch(id -> id.equals(Minecraft.getInstance().player.getUUID())))
                    .findAny().orElse(null);

            int[] completedByTeam = payload.goals().stream().mapToInt(Pair::getB).toArray();

            boolean previouslyStarted = lockout != null && lockout.hasStarted();
            long previousTicks = lockout != null ? lockout.getTicks() : 0;

            Minecraft client = context.client();
            try {
                List<Goal> goals = GoalRegistry.INSTANCE.constructGoals(payload.goals().stream().map(Pair::getA).toList());
                lockout = new Lockout(new LockoutBoard(goals), teams);
            } catch (IllegalGoalConstructionException e) {
                if(client.player != null) client.player.sendSystemMessage(Component.literal(e.getMessage()));
                Lockout.log(e.getMessage());
                return;
            }

            lockout.setRunning(payload.isRunning());
            lockout.setStarted(previouslyStarted);
            lockout.setTicks(previousTicks);

            goalHintResults.clear();
            goalHintErrors.clear();

            List<Goal> goalList = lockout.getBoard().getGoals();
            for (int i = 0; i < goalList.size(); i++) {
                if (completedByTeam[i] != -1) {
                    LockoutTeam team = lockout.getTeams().get(completedByTeam[i]);
                    goalList.get(i).setCompleted(true, team);
                    team.addPoint();
                }
            }

            client.execute(() -> {
                if (client.player != null && !previouslyStarted) {
                    client.gui.setScreen(new BoardScreen(BOARD_SCREEN_HANDLER.create(0, client.player.getInventory()), client.player.getInventory(), Component.empty()));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(StartLockoutPayload.ID, (payload, context) -> {
            lockout.setStarted(true);
            context.client().execute(() -> {
                if (Minecraft.getInstance().gui.screen() != null) {
                    Minecraft.getInstance().gui.screen().onClose();
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(UpdateTimerPayload.ID, (payload, context) -> {
            lockout.setTicks(payload.ticks());
        });
        ClientPlayNetworking.registerGlobalReceiver(CompleteTaskPayload.ID, (payload, context) -> {
            Minecraft client = context.client();
            client.execute(() -> {
                Optional<Goal> goalFinder = lockout.getBoard().getGoals().stream().filter(g -> g.getId().equals(payload.goal())).findFirst();
                if(goalFinder.isEmpty()) {
                    client.gui.hud.getChat().addClientSystemMessage(Component.literal(ChatFormatting.RED + "Received completion for unknown goal: " + payload.goal()));
                    return;
                }
                Goal goal = goalFinder.get();
                if (goal.isCompleted() || payload.teamIndex() == -1) {
                    lockout.clearGoalCompletion(goal, false);
                }
                if (payload.teamIndex() != -1) {
                    LockoutTeam team = lockout.getTeams().get(payload.teamIndex());
                    team.addPoint();
                    goal.setCompleted(true, lockout.getTeams().get(payload.teamIndex()));

                    if (client.player != null && playerTeam != null) {
                        if (team.getPlayerNames().contains(client.player.getName().getString())) {
                            client.player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 2f, 1f);
                        } else {
                            client.player.playSound(SoundEvents.GUARDIAN_DEATH, 2f, 1f);
                        }
                    }
                }
                if(payload.announce()) {
                    client.gui.hud.getChat().addClientSystemMessage(
                            Component.literal(ChatFormatting.GREEN + (payload.completedName() == null ? "Someone" : payload.completedName()) + " completed " + goal.extractName().getString() + ".")
                    );
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(HintResultPayload.ID, (payload, context) -> {
            if (payload.success()) {
                goalHintResults.computeIfAbsent(payload.goalId(), k -> new HashMap<>()).put(payload.hintIndex(), payload.message());
            } else {
                goalHintErrors.computeIfAbsent(payload.goalId(), k -> new HashMap<>()).put(payload.hintIndex(), payload.message());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(GoalProgressPayload.ID, (payload, context) -> {
            if (lockout == null) return;
            lockout.getBoard().getGoals().stream()
                    .filter(g -> g.getId().equals(payload.goalId()))
                    .findFirst()
                    .ifPresent(g -> g.setProgress(payload.progress()));
        });
        ClientPlayNetworking.registerGlobalReceiver(EndLockoutPayload.ID, (payload, context) -> {
            lockout.setRunning(false);
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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            {
                var commandNode = ClientCommands.literal("BoardPosition").build();
                var positionNode = ClientCommands.argument("board position", BoardPositionArgumentType.newInstance()).executes((context) -> {
                    String position = context.getArgument("board position", String.class);

                    LockoutConfig.BoardPosition boardPosition = LockoutConfig.BoardPosition.match(position);
                    if (boardPosition == null) {
                        context.getSource().sendError(Component.literal("Invalid board position: " + position + "."));
                        return 0;
                    }
                    LockoutConfig.getInstance().boardPosition = boardPosition;
                    LockoutConfig.save();

                    context.getSource().sendFeedback(Component.literal("Updated board position." + (boardPosition == LockoutConfig.BoardPosition.LEFT ? " Note: Opening debug hud (F3) will hide the board." : "")));

                    return 1;
                }).build();

                dispatcher.getRoot().addChild(commandNode);
                commandNode.addChild(positionNode);
            }
            {
                var commandNode = ClientCommands.literal("BoardBuilder").executes((context) -> {
                    Minecraft client = Minecraft.getInstance();
                    client.schedule(() -> {
                        if (client.player != null) {
                            client.gui.setScreen(new BoardBuilderScreen());
                        }
                    });

                    return 1;
                }).build();

                var boardNameNode = ClientCommands.argument("board name", CustomBoardFileArgumentType.newInstance()).executes((context) -> {
                    String boardName = context.getArgument("board name", String.class);

                    JSONBoard jsonBoard;
                    try {
                        jsonBoard = BoardBuilderIO.INSTANCE.readBoard(boardName);
                    } catch (IOException e) {
                        context.getSource().sendError(Component.literal("Error while trying to read board."));
                        return 0;
                    }

                    int size = (int) Math.sqrt(jsonBoard.goals.size());
                    if (size * size != jsonBoard.goals.size() || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
                        context.getSource().sendError(Component.literal("Board doesn't have a valid number of goals!"));
                        return 0;
                    }

                    List<Goal> goals = new ArrayList<>();
                    for(JSONBoard.JSONGoal goal : jsonBoard.goals) {
                        if(!GoalRegistry.INSTANCE.isRegistered(goal.id)) {
                            context.getSource().sendError(Component.literal("Goal id " + goal.id + " is not registered."));
                            return 0;
                        }
                        try {
                            goals.add(GoalRegistry.INSTANCE.get(goal.id).buildFromSerializedData(goal.data));
                        } catch (IllegalGoalConstructionException e) {
                            context.getSource().sendError(Component.literal("Failed to construct goal " + goal.id + ": " + e.getMessage()));
                            return 0;
                        }
                    }

/*
                    List<Pair<String, String>> goals = jsonBoard.goals.stream()
                            .map(goal -> new Pair<>(goal.id, goal.data != null ? goal.data : GoalDataConstants.DATA_NONE)).toList();
*/

                    Minecraft client = Minecraft.getInstance();
                    client.schedule(() -> {
                        if (client.player != null) {
                            BoardBuilderData.INSTANCE.setBoard(boardName, size, goals);
                            client.gui.setScreen(new BoardBuilderScreen());
                        }
                    });

                    return 1;
                }).build();

                commandNode.addChild(boardNameNode);
                dispatcher.getRoot().addChild(commandNode);
            }
            {
                var commandNode = ClientCommands.literal("SetCustomBoard").requires(ccs -> {
                    if (Minecraft.getInstance().isLocalServer()) {
                        return true;
                    }
                    return Permissions.check(ccs, PLACEHOLDER_PERM_STRING, LevelBasedPermissionSet.GAMEMASTER.level());
                }).build();

                var boardNameNode = ClientCommands.argument("board name", CustomBoardFileArgumentType.newInstance()).executes((context) -> {
                    String boardName = context.getArgument("board name", String.class);

                    JSONBoard jsonBoard;
                    try {
                        jsonBoard = BoardBuilderIO.INSTANCE.readBoard(boardName);
                    } catch (IOException e) {
                        context.getSource().sendError(Component.literal("Error while trying to read board."));
                        return 0;
                    }

                    int size = (int) Math.sqrt(jsonBoard.goals.size());
                    if (size * size != jsonBoard.goals.size() || size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
                        context.getSource().sendError(Component.literal("Board doesn't have a valid number of goals!"));
                        return 0;
                    }

                    ClientPlayNetworking.send(new CustomBoardPayload(Optional.of(jsonBoard.goals.stream()
                            .map(goal -> new Pair<>(goal.id, goal.data)).toList())));
                    return 1;
                }).build();

                commandNode.addChild(boardNameNode);
                dispatcher.getRoot().addChild(commandNode);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(LockoutVersionPayload.ID, (payload, context) -> {
            // Compare Lockout versions, disconnect if invalid.
            String version = payload.version();
            if (!version.equals(LockoutInitializer.MOD_VERSION.getFriendlyString())) {
                Minecraft.getInstance().player.connection.getConnection().disconnect(Component.nullToEmpty("Wrong Lockout version: v" + LockoutInitializer.MOD_VERSION.getFriendlyString() + ".\nServer is using Lockout v" + version + "."));
                return;
            }

            // Respond with version, it will be compared on server as well
            ClientPlayNetworking.send(new LockoutVersionPayload(LockoutInitializer.MOD_VERSION.getFriendlyString()));
        });
        
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lockout.open_board", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_B, // The keycode of the key
                LOCKOUT_CATEGORY // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CURRENT_TICK++;

            boolean wasPressed = false;
            while (keyBinding.consumeClick()) {
                wasPressed = true;
            }
            if (wasPressed) {
                if (client.gui.screen() != null || client.player == null) {
                    return;
                }

                // If the game hasn't started, open board builder instead
                if (!Lockout.exists(lockout)) {
                    client.gui.setScreen(new BoardBuilderScreen());
                    return;
                }

                // Open GUI
                client.gui.setScreen(new BoardScreen(BOARD_SCREEN_HANDLER.create(0, client.player.getInventory()), client.player.getInventory(), Component.empty()));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            lockout = null;
            allAdvancements.clear();
            goalHintResults.clear();
            goalHintErrors.clear();
        }));

        MenuScreens.register(BOARD_SCREEN_HANDLER, BoardScreen::new);
    }

}

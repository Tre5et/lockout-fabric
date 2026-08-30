package me.marin.lockout.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.network.HintResultPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.TeamColor;

import java.io.IOException;
import java.util.*;

public class ServerLockoutTeam extends LockoutTeam {
    @Getter
    private final MinecraftServer server;
    @Getter
    private final Map<String, Map<Integer, String>> resolvedHints = new HashMap<>();

    public ServerLockoutTeam(List<String> playerNames, TeamColor formattingColor, MinecraftServer server) {
        this(playerNames, playerNames.stream().map(n -> server.getPlayerList().getPlayerByName(n).getUUID()).toList(), formattingColor, server);
    }

    public ServerLockoutTeam(List<String> playerNames, List<UUID> playerIds, TeamColor formattingColor, MinecraftServer server) {
        super(playerNames, playerIds, formattingColor);
        this.server = server;
    }

    public void sendMessage(String message) {
        for (UUID uuid : getPlayerIds()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.sendSystemMessage(Component.literal(message));
            }
        }
    }

    public void storeHintResult(String goalId, int hintIndex, String data) {
        resolvedHints.computeIfAbsent(goalId, _ -> new HashMap<>()).put(hintIndex, data);
    }

    public void sendStoredHints(ServerPlayer player) {
        resolvedHints.forEach((goalId, hints) ->
                hints.forEach((hintIndex, data) ->
                        ServerPlayNetworking.send(player, new HintResultPayload(goalId, hintIndex, Optional.ofNullable(data), Optional.empty()))));
    }

    @Override
    public JsonElement serialize() {
        JsonObject object = super.serialize().getAsJsonObject();
        JsonObject hints = new JsonObject();
        resolvedHints.forEach((t, h) -> {
            JsonObject hint = new JsonObject();
            h.forEach((i, v) -> hint.add(i.toString(), new JsonPrimitive(v)));
            hints.add(t, hint);
        });
        object.add("hints", hints);
        return object;
    }

    public static ServerLockoutTeam deserialize(JsonElement element, MinecraftServer server) throws IOException {
        LockoutTeam genericTeam = LockoutTeam.deserialize(element);
        JsonObject object = element.getAsJsonObject();
        ServerLockoutTeam team = new ServerLockoutTeam(genericTeam.getPlayerNames(), genericTeam.getPlayerIds(), genericTeam.getColor(), server);

        if(object.has("hints")) {
            if(!object.get("hints").isJsonObject()) throw new IOException("Server board hints is not a valid json object.");
            JsonObject hints = object.getAsJsonObject("hints");

            Map<String, Map<Integer, String>> newHints = new HashMap<>();
            for(Map.Entry<String, JsonElement> goalHints : hints.entrySet()) {
                if(!goalHints.getValue().isJsonObject()) throw new IOException("Server board goal hints is not a valid json object.");
                Map<Integer, String> newGoalHints = new HashMap<>();
                for(Map.Entry<String, JsonElement> indexHint : goalHints.getValue().getAsJsonObject().entrySet()) {
                    if(!indexHint.getValue().isJsonPrimitive() || !indexHint.getValue().getAsJsonPrimitive().isString()) throw new IOException("Server board hint is not a string.");
                    newGoalHints.put(Integer.valueOf(indexHint.getKey()), indexHint.getValue().getAsString());
                }
                newHints.put(goalHints.getKey(), newGoalHints);
            }
            team.getResolvedHints().clear();
            team.getResolvedHints().putAll(newHints);
        }
        return team;
    }
}

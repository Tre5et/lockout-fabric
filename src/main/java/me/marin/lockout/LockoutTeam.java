package me.marin.lockout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.TeamColor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockoutTeam {

    private final List<String> players;
    @Getter
    private final List<UUID> playerIds;
    @Getter
    private final TeamColor color;
    @Getter
    @Setter
    private int points = 0;
    @Getter
    @Setter
    private boolean forfeited = false;

    public LockoutTeam(List<String> playerNames, TeamColor formattingColor) {
        this(playerNames, new ArrayList<>(), formattingColor);
    }

    public LockoutTeam(List<String> playerNames, List<UUID> playerIds, TeamColor formattingColor) {
        this.players = playerNames;
        this.playerIds = playerIds;
        this.color = formattingColor;
    }

    public List<String> getPlayerNames() {
        return players;
    }

    public boolean containsPlayer(UUID uuid) {
        return playerIds.contains(uuid);
    }

    public String getDisplayName() {
        String name = players.size() == 1 ? players.get(0) : "Team " + formattingToString(color);
        return forfeited ? name + " (Forfeited)" : name;
    }

    public void addPoint() {
        this.points++;
    }
    public void takePoint() {
        this.points--;
    }

    public ChatFormatting getChatFormatting() {
        return ChatFormatting.valueOf(getColor().textColor().serialize().toUpperCase());
    }

    public static String formattingToString(TeamColor formatting) {
        return StringUtils.capitalize(formatting.getSerializedName().replace("_", " "));
    }

    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        JsonArray playerNames = new JsonArray();
        players.forEach(p -> playerNames.add(new JsonPrimitive(p)));
        object.add("playerNames", playerNames);
        JsonArray playerIdData = new JsonArray();
        playerIds.forEach(p -> playerIdData.add(new JsonPrimitive(p.toString())));
        object.add("playerIds", playerIdData);
        object.add("color", new JsonPrimitive(color.getSerializedName()));
        object.add("points", new JsonPrimitive(points));
        object.add("forfeited", new JsonPrimitive(forfeited));
        return object;
    }

    public static LockoutTeam deserialize(JsonElement json) throws IOException {
        if(!json.isJsonObject()) throw new IOException("Team data is not valid");
        JsonObject data = json.getAsJsonObject();

        JsonElement playerNameData = data.get("playerNames");
        if(playerNameData == null || !playerNameData.isJsonArray()) throw new IOException("Team data does not contain valid player names.");
        List<String> playerNames = new ArrayList<>();
        for(JsonElement element : playerNameData.getAsJsonArray()) {
            if(!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) throw new IOException("Team player name data is not a string.");
            playerNames.add(element.getAsString());
        }

        JsonElement playerIdData = data.get("playerIds");
        if(playerIdData == null || !playerIdData.isJsonArray()) throw new IOException("Team data does not contain valid player ids.");
        List<UUID> playerIds = new ArrayList<>();
        for(JsonElement element : playerIdData.getAsJsonArray()) {
            if(!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) throw new IOException("Team player id data is not a string.");
            playerIds.add(UUID.fromString(element.getAsString()));
        }

        JsonElement colorData = data.get("color");
        if(colorData == null || !colorData.isJsonPrimitive() || !colorData.getAsJsonPrimitive().isString()) throw new IOException("Team data does not contain a valid color.");
        TeamColor color = TeamColor.byName(colorData.getAsString());

        LockoutTeam team = new LockoutTeam(playerNames, playerIds, color);

        JsonElement points = data.get("points");
        if(points == null || !points.isJsonPrimitive() || !points.getAsJsonPrimitive().isNumber()) throw new IOException("Team data does not contain valid points.");
        team.setPoints(points.getAsInt());

        JsonElement forfeited = data.get("forfeited");
        if(forfeited == null || !forfeited.isJsonPrimitive() || !forfeited.getAsJsonPrimitive().isBoolean()) throw new IOException("Team data does not contain valid forfeited.");
        team.setForfeited(forfeited.getAsBoolean());

        return team;
    }
}

package me.marin.lockout;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.TeamColor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockoutTeam {

    private final List<String> players;
    private final List<UUID> playerIds;
    @Getter
    private final TeamColor color;
    @Getter
    private int points = 0;
    @Getter
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

    public List<UUID> getPlayerIds() {
        return playerIds;
    }

    public boolean containsPlayer(UUID uuid) {
        return playerIds.contains(uuid);
    }

    public String getDisplayName() {
        String name = players.size() == 1 ? players.get(0) : "Team " + formattingToString(color);
        return forfeited ? name + " (Forfeited)" : name;
    }

    public void setForfeited(boolean forfeited) {
        this.forfeited = forfeited;
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

}

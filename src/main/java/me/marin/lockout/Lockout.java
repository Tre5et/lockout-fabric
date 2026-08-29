package me.marin.lockout;

import com.google.gson.*;
import net.minecraft.world.scores.TeamColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Lockout {

    private static final Logger logger = LogManager.getLogger("Lockout");
    public static final TeamColor[] COLOR_ORDERS = new TeamColor[]{TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW, TeamColor.GOLD, TeamColor.LIGHT_PURPLE, TeamColor.AQUA, TeamColor.DARK_PURPLE, TeamColor.DARK_AQUA, TeamColor.DARK_GREEN, TeamColor.WHITE, TeamColor.DARK_RED, TeamColor.GRAY, TeamColor.DARK_BLUE, TeamColor.DARK_GRAY, TeamColor.BLACK};
    public static void log(String message) {
        logger.log(Level.INFO, message);
    }

    public static void error(Throwable t) {
        logger.error("Lockout error:\n", t);
    }

    private static String getWinnerTeamsString(List<? extends LockoutTeam> teams) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < teams.size(); i++) {
            if (i > 0) {
                if (i + 1 == teams.size()) {
                    sb.append(" and ");
                } else {
                    sb.append(", ");
                }
            }
            LockoutTeam team = teams.get(i);
            sb.append(team.getDisplayName());
        }
        return sb.toString();
    }
}

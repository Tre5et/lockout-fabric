package me.marin.lockout.game;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.LockoutTeam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class LockoutGame<T extends LockoutBoard<?>> {
    @Getter
    protected final T board;
    @Getter
    protected final List<? extends LockoutTeam> teams;
    @Getter
    @Setter
    protected GameState state = GameState.STARTING;
    @Getter
    @Setter
    protected long ticks;

    public LockoutGame(T board, List<? extends LockoutTeam> teams) {
        this.board = board;
        this.teams = teams;
    }

    public String getModeName() {
        return teams.size() > 1 ? "Lockout" : "Blackout";
    }

    public boolean isSoloBlackout() {
        return teams.size() == 1 && teams.getFirst().getPlayerNames().size() == 1;
    }

    public LockoutTeam getPlayerTeam(UUID playerId) {
        for (LockoutTeam team : teams) {
            if (team.containsPlayer(playerId)) {
                return team;
            }
        }
        return null;
    }

    public static boolean isActive(LockoutGame<?> game) {
        return game != null && game.getState().isActive();
    }

    public Map<LockoutTeam, Integer> getPoints() {
        Map<LockoutTeam, Integer> points = new HashMap<>(getTeams().stream().collect(Collectors.toMap(t -> t, _ -> 0)));
        getBoard().getGoals()
                .forEach(g -> g.getProgress().getCompletedTeams(this)
                        .forEach(t -> points.put(t, points.get(t) + 1))
                );
        return points;
    }
}

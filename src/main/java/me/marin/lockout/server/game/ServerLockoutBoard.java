package me.marin.lockout.server.game;

import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutBoard;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.goal.ServerGoal;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public class ServerLockoutBoard extends LockoutBoard<ServerGoal<?>> {
    public ServerLockoutBoard(List<ServerGoal<?>> goals) {
        super(goals);
    }

    public void update(Object data, Player player) {
        Optional<? extends LockoutTeam> team = LockoutServer.lockout.getTeams().stream()
                .filter(t -> t.containsPlayer(player.getUUID()))
                .findAny();
        if(team.isEmpty()) return;

        for(ServerGoal<?> goal : getGoals()) {
            if(goal == null) continue;
            if(goal.hasAnyCompleted()) continue;
            goal.updateProgressUnchecked(team.get(), data, LockoutServer.lockout);
        }
    }
}

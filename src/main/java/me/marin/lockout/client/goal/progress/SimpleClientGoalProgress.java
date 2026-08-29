package me.marin.lockout.client.goal.progress;

import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.progress.SimpleGoalProgress;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SimpleClientGoalProgress extends SimpleGoalProgress implements ClientGoalProgress<Boolean> {
    @Override
    public Component getTitle() {
        return Component.literal("Completed");
    }

    @Override
    public Component getDisplayString(Boolean value) {
        return Component.literal(value ? "yes" : "no");
    }

    @Override
    public List<Component> getTooltip(LockoutTeam team, LockoutGame<?> lockout) {
        return List.of();
    }
}

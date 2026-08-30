package me.marin.lockout.client.goal.progress;

import me.marin.lockout.LockoutTeam;
import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.progress.GoalProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public interface ClientGoalProgress<T> extends GoalProgress<T> {
    Component getTitle();
    Component getDisplayString(T value);

    default Component getTeamDisplay(LockoutTeam team) {
        return Component.literal(team.getDisplayName());
    }

    default List<Component> getTooltip(LockoutTeam team, LockoutGame<?> lockout) {
        return List.of(Component.empty().append(getTitle()).append(": ").append(getDisplayString(getProgress(lockout.getTeams().indexOf(team)))));
    }

    default List<Component> getSpectatorTooltip(LockoutGame<?> lockout) {
        List<Component> components = new ArrayList<>(List.of(Component.empty().append(getTitle()).append(":")));
        for(int i = 0; i < lockout.getTeams().size(); i++) {
            components.add(Component.empty()
                    .append(Component.literal("- "))
                    .append(getTeamDisplay(lockout.getTeams().get(i)))
                    .append(Component.literal(": "))
                    .append(getDisplayString(getProgress(i)))
            );
        }
        return components;
    }

    default void announceCompletion(LockoutTeam team, Minecraft minecraft, Component goalName) {
        minecraft.gui.hud.getChat().addClientSystemMessage(
                Component.empty().withColor(TextColor.GREEN)
                        .append(Component.literal(team.getDisplayName() + " completed "))
                        .append(goalName)
        );
    }
}

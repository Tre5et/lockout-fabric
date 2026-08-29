package me.marin.lockout.client.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetNumberGoalProgress;
import net.minecraft.network.chat.Component;

public class TargetNumberClientGoalProgress extends TargetNumberGoalProgress implements ClientGoalProgress<Integer> {
    private final String title;

    public TargetNumberClientGoalProgress(String title, int target) {
        super(target);
        this.title = title;
    }

    @Override
    public Component getTitle() {
        return Component.literal(title);
    }

    @Override
    public Component getDisplayString(Integer value) {
        return Component.literal(value + " / " + getTarget());
    }
}

package me.marin.lockout.client.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetFloatGoalProgress;
import net.minecraft.network.chat.Component;

public class TargetFloatClientGoalProgress extends TargetFloatGoalProgress implements ClientGoalProgress<Float> {
    private final String title;

    public TargetFloatClientGoalProgress(String title, Float target) {
        super(target);
        this.title = title;
    }

    @Override
    public Component getTitle() {
        return Component.literal(title);
    }

    @Override
    public Component getDisplayString(Float value) {
        return Component.literal(String.format("%.1f / %.1f", value, getTarget()));
    }
}

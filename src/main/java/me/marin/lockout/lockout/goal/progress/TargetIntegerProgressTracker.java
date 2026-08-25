package me.marin.lockout.lockout.goal.progress;

import net.minecraft.network.chat.Component;

public class TargetIntegerProgressTracker extends IntegerProgressTracker {
    private final int target;

    public TargetIntegerProgressTracker(Component title, int target) {
        super(title);
        this.target = target;
    }

    @Override
    protected String displayString(Integer value) {
        return Math.min(value, target) + " / " + target;
    }
}

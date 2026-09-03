package me.marin.lockout.client.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetFloatGoalProgress;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class TargetFloatClientGoalProgress extends TargetFloatGoalProgress implements ClientGoalProgress<Number> {
    private final String title;
    private final Function<Number, String> numberToString;

    public TargetFloatClientGoalProgress(String title, Number target, Function<Number, String> numberToString) {
        super(target);
        this.title = title;
        this.numberToString = numberToString;
    }

    @Override
    public Component getTitle() {
        return Component.literal(title);
    }

    @Override
    public Component getDisplayString(Number value) {
        return Component.literal(numberToString.apply(value) + " / " + numberToString.apply(getTarget()));
    }
}

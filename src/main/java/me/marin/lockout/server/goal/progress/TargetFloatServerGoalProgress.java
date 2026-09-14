package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetFloatGoalProgress;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

public class TargetFloatServerGoalProgress<T> extends TargetFloatGoalProgress implements ServerGoalProgress<T, Number> {
    private final BiFunction<T, ServerPlayer,Number> updateFunction;

    public TargetFloatServerGoalProgress(Number target, BiFunction<T, ServerPlayer, Number> updateFunction) {
        super(target);
        this.updateFunction = updateFunction;
    }

    @Override
    public Number update(Number current, T update, ServerPlayer player) {
        return current.doubleValue() + updateFunction.apply(update, player).doubleValue();
    }
}

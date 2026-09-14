package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.TargetNumberGoalProgress;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiPredicate;

public class TargetNumberServerGoalProgress<T> extends TargetNumberGoalProgress implements ServerGoalProgress<T,Integer> {
    private final BiPredicate<T, ServerPlayer> updatePredicate;

    public TargetNumberServerGoalProgress(int target, BiPredicate<T,ServerPlayer> updatePredicate) {
        super(target);
        this.updatePredicate = updatePredicate;
    }

    @Override
    public Integer update(Integer current, T update, ServerPlayer player) {
        if(updatePredicate.test(update, player)) {
            return current + 1;
        }
        return current;
    }
}

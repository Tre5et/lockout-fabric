package me.marin.lockout.server.goal.progress;

import me.marin.lockout.lockout.goal.progress.SimpleGoalProgress;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiPredicate;

public class SimpleServerGoalProgress<U> extends SimpleGoalProgress implements ServerGoalProgress<U,Boolean> {
    private final BiPredicate<U,ServerPlayer> satisfiedPredicate;

    public SimpleServerGoalProgress(BiPredicate<U,ServerPlayer> satisfiedPredicate) {
        this.satisfiedPredicate = satisfiedPredicate;
    }

    @Override
    public Boolean update(Boolean current, U update, ServerPlayer player) {
        return current || satisfiedPredicate.test(update, player);
    }
}

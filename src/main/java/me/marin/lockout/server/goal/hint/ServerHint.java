package me.marin.lockout.server.goal.hint;

import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.Hint;
import me.marin.lockout.network.HintResultPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerHint<T> extends Hint<T> {
    GoalHintResult<T> resolve(MinecraftServer server, ServerPlayer player);

    default HintResultPayload resolvePayload(String goalId, int hintIndex, MinecraftServer server, ServerPlayer player) {
        return resolve(server, player).getPayload(goalId, hintIndex, this::serialize);
    }
}

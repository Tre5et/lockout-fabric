package me.marin.lockout.server.goal.hint;

import com.google.gson.JsonParser;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.Hint;
import me.marin.lockout.network.HintResultPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerHint<T> extends Hint<T> {
    GoalHintResult<T> resolve(MinecraftServer server, ServerPlayer player);

    void onPropagateToPlayer(T result, MinecraftServer server, ServerPlayer player);
    default void onPropagateToPlayerPayload(HintResultPayload result, MinecraftServer server, ServerPlayer player) {
        if(result.data().isEmpty()) return;
        this.onPropagateToPlayer(this.deserialize(JsonParser.parseString(result.data().get())), server, player);
    }

    default HintResultPayload resolvePayload(String goalId, int hintIndex, MinecraftServer server, ServerPlayer player) {
        return resolve(server, player).getPayload(goalId, hintIndex, this::serialize);
    }
}

package me.marin.lockout.server.goal.hint;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.Hint;
import me.marin.lockout.network.HintResultPayload;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerHint<T> extends Hint<T> {
    Codec<ServerHint<?>> CODEC = LockoutRegistries.SERVER_HINT_TYPE.byNameCodec().dispatch(ServerHint::codec, c -> c);

    MapCodec<? extends ServerHint<?>> codec();

    GoalHintResult<T> resolve(MinecraftServer server, ServerPlayer player);

    static void bootstrap(Registry<MapCodec<? extends ServerHint<?>>> registry) {
        LockoutRegistries.register(registry, "biome", BiomeServerHint.MAP_CODEC);
        LockoutRegistries.register(registry, "structure", StructureServerHint.MAP_CODEC);
    }

    void onPropagateToPlayer(T result, MinecraftServer server, ServerPlayer player);
    default void onPropagateToPlayerPayload(HintResultPayload result, MinecraftServer server, ServerPlayer player) {
        if(result.data().isEmpty()) return;
        this.onPropagateToPlayer(this.deserialize(JsonParser.parseString(result.data().get())), server, player);
    }

    default HintResultPayload resolvePayload(ResourceKey<GoalBuilder<?,?>> goalId, int hintIndex, MinecraftServer server, ServerPlayer player) {
        return resolve(server, player).getPayload(goalId, hintIndex, this::serialize);
    }
}

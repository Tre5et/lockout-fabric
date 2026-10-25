package me.marin.lockout.lockout.goal.hint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.server.goal.hint.ServerHint;

public record HintCombination<T>(
        HintProvider<ServerHint<T>, T> server,
        HintProvider<ClientHint<T>, T> client
) {
    public static final Codec<HintCombination<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ServerHint.CODEC.fieldOf("server").forGetter(c -> c.server().provide()),
            ClientHint.CODEC.fieldOf("client").forGetter(c -> c.client().provide())
    ).apply(instance, (s,c) -> new HintCombination<>(() -> (ServerHint<Object>) s, () -> (ClientHint<Object>) c)));

    public ServerHint<T> constructServer() {
        return server().provide();
    }

    public ClientHint<T> constructClient() {
        return client().provide();
    }
}

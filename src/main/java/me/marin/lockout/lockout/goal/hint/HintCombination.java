package me.marin.lockout.lockout.goal.hint;

import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.server.goal.hint.ServerHint;

public record HintCombination<T>(
        HintProvider<ServerHint<T>, T> server,
        HintProvider<ClientHint<T>, T> client
) {
    public ServerHint<T> constructServer() {
        return server().provide();
    }

    public ClientHint<T> constructClient() {
        return client().provide();
    }
}

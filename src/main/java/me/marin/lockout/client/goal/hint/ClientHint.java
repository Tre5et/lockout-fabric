package me.marin.lockout.client.goal.hint;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.Hint;
import me.marin.lockout.network.HintResultPayload;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public interface ClientHint<T> extends Hint<T> {
    Codec<ClientHint<?>> CODEC = LockoutRegistries.CLIENT_HINT_TYPE.byNameCodec().dispatch(ClientHint::codec, c -> c);

    Component getTitle();
    T getData();
    void setData(T data);
    String getError();
    void setError(String message);

    MapCodec<? extends ClientHint<?>> codec();

    static void bootstrap(Registry<MapCodec<? extends ClientHint<?>>> registry) {
        LockoutRegistries.register(registry, "position", PositionClientHint.MAP_CODEC);
    }

    default void update(GoalHintResult<T> result) {
        setError(result.getError());
        setData(result.getData());
    }

    default void updatePayload(HintResultPayload payload) throws IllegalArgumentException {
        update(new GoalHintResult<>(
                payload.data().map(d -> deserialize(JsonParser.parseString(d))).orElse(null),
                payload.error().orElse(null)
        ));
    }

    default Component extract(KeyMapping key) {
        String error = getError();
        if(error != null) return extractError(error);
        T data = getData();
        if(data == null) return extractUnresolved(key);
        return Component.empty().withColor(TextColor.AQUA).append(extractResolved(data, key));
    }

    default Component extractUnresolved(KeyMapping key) {
        return Component.empty().append(getTitle()).append(Component.literal(": Press ").append(key.getTranslatedKeyMessage()));
    }

    default Component extractError(String error) {
        return Component.empty().withColor(TextColor.RED).append(getTitle()).append(" ERROR: ").append(error);
    }

    Component extractResolved(T data, KeyMapping key);
}

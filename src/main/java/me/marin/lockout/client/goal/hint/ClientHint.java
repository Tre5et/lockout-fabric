package me.marin.lockout.client.goal.hint;

import com.google.gson.JsonParser;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.Hint;
import me.marin.lockout.network.HintResultPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public interface ClientHint<T> extends Hint<T> {
    Component getTitle();
    Component extractData(T data);
    T getData();
    void setData(T data);
    String getError();
    void setError(String message);

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
        return extractResolved(data);
    }

    default Component extractUnresolved(KeyMapping key) {
        return Component.empty().append(getTitle()).append(Component.literal(": Press ").append(key.getTranslatedKeyMessage()));
    }

    default Component extractError(String error) {
        return Component.empty().withColor(TextColor.RED).append(getTitle()).append(" ERROR: ").append(error);
    }

    default Component extractResolved(T data) {
        return Component.empty().withColor(TextColor.AQUA).append(getTitle()).append(" ").append(extractData(data));
    }
}

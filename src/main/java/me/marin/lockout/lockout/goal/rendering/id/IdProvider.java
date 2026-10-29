package me.marin.lockout.lockout.goal.rendering.id;

import com.mojang.serialization.Codec;

public interface IdProvider {
    Codec<IdProvider> CODEC = Codec.stringResolver(p -> p.get(null), FormatString::new);

    String get(Object option);

    record FormatString(String string) implements IdProvider {
        @Override
        public String get(Object option) {
            return String.format(string, option);
        }
    }
}

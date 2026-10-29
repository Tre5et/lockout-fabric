package me.marin.lockout.lockout.goal.rendering.name;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public interface NameProvider {
    Codec<NameProvider> CODEC = Codec.stringResolver(p -> p.get(null).getString(), FormatString::new);

    Component get(Object option);

    record FormatString(String string) implements NameProvider {
        @Override
        public Component get(Object option) {
            return Component.literal(String.format(string, option));
        }
    }
}

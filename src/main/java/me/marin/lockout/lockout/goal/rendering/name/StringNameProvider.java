package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

public interface StringNameProvider<T> extends NameProvider<T> {
    String getString(T option);

    @Override
    default Component get(T option) {
        return Component.literal(getString(option));
    }
}

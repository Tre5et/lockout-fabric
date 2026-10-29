package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

public interface StringNameProvider extends NameProvider {
    String getString(Object option);

    @Override
    default Component get(Object option) {
        return Component.literal(getString(option));
    }
}

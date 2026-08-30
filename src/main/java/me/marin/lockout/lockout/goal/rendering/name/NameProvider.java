package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

public interface NameProvider<T> {
    Component get(T option);
}

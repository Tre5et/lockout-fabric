package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface NameExtractor {
    Component extract();

    static StaticNameExtractor simple(String name) {
        return new StaticNameExtractor(name);
    }

    static ConstructedNameExtractor simple(Supplier<String> constructor) {
        return new ConstructedNameExtractor(constructor);
    }
}

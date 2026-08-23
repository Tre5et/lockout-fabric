package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ConstructedNameExtractor implements NameExtractor {
    private final Supplier<String> constructor;
    private Component name;

    public ConstructedNameExtractor(Supplier<String> constructor) {
        this.constructor = constructor;
    }


    @Override
    public Component extract() {
        if(name == null) {
            name = Component.literal(constructor.get());
        }
        return name;
    }
}

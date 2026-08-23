package me.marin.lockout.lockout.goal.rendering.name;

import net.minecraft.network.chat.Component;

public class StaticNameExtractor implements NameExtractor {
    private final Component name;

    public StaticNameExtractor(String name) {
        this.name = Component.literal(name);
    }

    @Override
    public Component extract() {
        return name;
    }
}

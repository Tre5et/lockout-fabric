package me.marin.lockout.client.goal.hint;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.lockout.goal.hint.PositionHint;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class PositionClientHint extends PositionHint implements ClientHint<BlockPos> {
    private final String name;
    @Getter
    @Setter
    private BlockPos data = null;
    @Getter
    @Setter
    private String error = null;

    public PositionClientHint(String name) {
        this.name = name;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Nearest " + name);
    }

    @Override
    public Component extractData(BlockPos data) {
        return Component.literal("at " + data.toShortString());
    }
}

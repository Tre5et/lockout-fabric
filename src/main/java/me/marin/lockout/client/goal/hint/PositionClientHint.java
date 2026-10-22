package me.marin.lockout.client.goal.hint;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.ClientConfig;
import me.marin.lockout.lockout.goal.hint.PositionHint;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import oshi.util.tuples.Pair;

public class PositionClientHint extends PositionHint implements ClientHint<Pair<BlockPos, Identifier>> {
    private final String name;
    @Getter
    @Setter
    private Pair<BlockPos,Identifier> data = null;
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
    public Component extractResolved(Pair<BlockPos,Identifier> data, KeyMapping key) {
        return switch(ClientConfig.getInstance().getServerConfig().getHintMode()) {
            case COMPASS -> Component.literal("Your Team received compasses for ").append(getTitle()).append(Component.literal(": Press ")).append(key.getTranslatedKeyMessage()).append(" to get updated compasses");
            case MAP -> Component.literal("Your Team received maps for ").append(getTitle()).append(Component.literal(": Press ")).append(key.getTranslatedKeyMessage()).append(" to get updated maps");
            default -> Component.empty().append(getTitle()).append(": ").append(data.getA().toShortString()).append("; Press ").append(key.getTranslatedKeyMessage()).append(" to update");
        };
    }
}

package me.marin.lockout.game;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum GameMode {
    LOCKOUT(false),
    BLACKOUT(true);

    public static final StreamCodec<ByteBuf, GameMode> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(GameMode::name, GameMode::valueOf));

    @Getter
    private final boolean isCoop;

    GameMode(boolean isCoop) {
        this.isCoop = isCoop;
    }
}

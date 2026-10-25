package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum GameDifficulty {
    RANDOM(DifficultyCheckFunction.allowAll(), DifficultyCheckFunction.allowAll()),
    EASY(DifficultyCheckFunction.absoluteDistance(0.0f, 1.8f).allowRange(0f, 0.6f), DifficultyCheckFunction.absoluteDistance(0.2f, 5f, 1/2f).allowRange(0.1f, 0.4f)),
    NORMAL(DifficultyCheckFunction.absoluteDistance(0.5f, 3f).allowRange(0.2f, 0.8f), DifficultyCheckFunction.absoluteDistance(0.5f, 5f, 1/2f).allowRange(0.35f, 0.65f)),
    HARD(DifficultyCheckFunction.absoluteDistance(1f, 1.8f).allowRange(0.4f, 1f), DifficultyCheckFunction.absoluteDistance(0.8f, 5f, 1/2f).allowRange(0.6f, 0.9f));

    public static final StreamCodec<ByteBuf, GameDifficulty> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(GameDifficulty::name, GameDifficulty::valueOf));


    @Getter
    private final DifficultyCheckFunction individual;
    @Getter
    private final DifficultyCheckFunction total;

    GameDifficulty(DifficultyCheckFunction individual, DifficultyCheckFunction total) {
        this.individual = individual;
        this.total = total;
    }
}

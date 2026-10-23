package me.marin.lockout.lockout.goal.difficulty;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum GameDifficulty {
    RANDOM(DifficultyProbabilityFunction.random()),
    EASY(DifficultyProbabilityFunction.absoluteDistance(0.2f, 3f).allowRange(0f, 0.6f)),
    MEDIUM(DifficultyProbabilityFunction.absoluteDistance(0.5f, 3f).allowRange(0.2f, 0.8f)),
    HARD(DifficultyProbabilityFunction.absoluteDistance(0.8f, 3f).allowRange(0.4f, 1f));

    public static final StreamCodec<ByteBuf, GameDifficulty> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(GameDifficulty::name, GameDifficulty::valueOf));


    @Getter
    private final DifficultyProbabilityFunction probabilityFunction;

    GameDifficulty(DifficultyProbabilityFunction probabilityFunction) {
        this.probabilityFunction = probabilityFunction;
    }
}

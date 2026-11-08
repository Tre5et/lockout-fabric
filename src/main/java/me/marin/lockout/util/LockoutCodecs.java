package me.marin.lockout.util;

import com.mojang.serialization.Codec;

import java.util.List;

public class LockoutCodecs {
    public static final Codec<String> NUMBER_OR_STRING = Codec.withAlternative(
            Codec.STRING,
            Codec.INT,
            Object::toString
    );

    public static <T> Codec<List<T>> list(Codec<T> codec) {
        return Codec.withAlternative(
                codec.listOf(),
                codec,
                List::of
        );
    }
}

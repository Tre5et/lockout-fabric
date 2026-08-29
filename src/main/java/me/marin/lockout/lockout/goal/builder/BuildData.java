package me.marin.lockout.lockout.goal.builder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record BuildData(
        String id,
        Optional<String> option
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BuildData> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BuildData::id,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            BuildData::option,
            BuildData::new
    );
}

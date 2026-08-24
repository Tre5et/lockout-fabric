package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record AllAdvancementsPayload(List<AdvancementHolder> advancements) implements CustomPacketPayload {
    public static final Type<AllAdvancementsPayload> ID = new Type<>(Constants.ALL_ADVANCEMENTS_PACKET);

    public static final StreamCodec<RegistryFriendlyByteBuf, AllAdvancementsPayload> CODEC = StreamCodec.composite(
            AdvancementHolder.LIST_STREAM_CODEC,
            AllAdvancementsPayload::advancements,
            AllAdvancementsPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

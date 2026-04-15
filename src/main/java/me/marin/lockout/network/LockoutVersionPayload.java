package me.marin.lockout.network;

import me.marin.lockout.Constants;
import me.marin.lockout.LockoutInitializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LockoutVersionPayload(String version) implements CustomPacketPayload {

    public static final Type<LockoutVersionPayload> ID = new Type<>(Constants.LOCKOUT_VERSION_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockoutVersionPayload> CODEC = StreamCodec.composite(
            StreamCodec.ofMember((version, buf) -> buf.writeUtf(LockoutInitializer.MOD_VERSION.getFriendlyString()), FriendlyByteBuf::readUtf),
            LockoutVersionPayload::version,
            LockoutVersionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}

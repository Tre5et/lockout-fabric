package me.marin.lockout;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public class ChatManager {

    public enum Type {
        LOCAL,
        TEAM;

        public static final StreamCodec<ByteBuf, Type> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(Type::name, Type::valueOf));
    }

    private static final Map<UUID, Type> map = new HashMap<>();

    public static void setChat(Player player, Type type) {
        map.put(player.getUUID(), type);
    }

    public static Type getChat(Player player) {
        return map.getOrDefault(player.getUUID(), Type.LOCAL);
    }

}

package me.marin.lockout.network;

import me.marin.lockout.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CustomBoardPayload(Optional<List<Pair<String, String>>> boardOrClear) implements CustomPacketPayload {
    public static final Type<CustomBoardPayload> ID = new Type<>(Constants.CUSTOM_BOARD_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomBoardPayload> CODEC = StreamCodec.composite(StreamCodec.ofMember(
            (boardOrClear, buf) -> {
                buf.writeInt(boardOrClear.map(pairs -> (int) Math.sqrt(pairs.size())).orElse(0));
                if (boardOrClear.isEmpty()) return;
                var board = boardOrClear.get();
                for (var goal : board) {
                    buf.writeUtf(goal.getA());
                    if(goal.getB() != null) {
                        buf.writeUtf(goal.getB());
                    } else {
                        buf.writeUtf("null");
                    }
                }
            },
            (buf) -> {
                int size = buf.readInt();
                if (size == 0) return Optional.empty();
                List<Pair<String, String>> goals = new ArrayList<>();
                for (int i = 0; i < size * size; i++) {
                    String goalId = buf.readUtf();
                    goals.add(new Pair<>(goalId, buf.readUtf()));
                }
                return Optional.of(goals);
            }
    ), CustomBoardPayload::boardOrClear, CustomBoardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

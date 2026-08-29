package me.marin.lockout.server.goal.hint;

import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import me.marin.lockout.lockout.goal.hint.PositionHint;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public abstract class PositionServerHint extends PositionHint implements ServerHint<BlockPos> {
    private final List<ResourceKey<Level>> applicableLevels;

    public PositionServerHint(List<ResourceKey<Level>> applicableLevels) {
        this.applicableLevels = applicableLevels;
    }

    protected abstract GoalHintResult<BlockPos> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level);

    @Override
    public GoalHintResult<BlockPos> resolve(MinecraftServer server, ServerPlayer player) {
        Optional<ResourceKey<Level>> level = applicableLevels.stream().filter(player.level().dimension()::equals).findAny();
        if(level.isEmpty()) {
            return GoalHintResult.error("You are not in the correct dimension.");
        }

        ServerLevel serverLevel = server.getLevel(level.get());
        if(serverLevel == null) {
            return GoalHintResult.error("The current player dimension " + level.get() + " does not exist.");
        }
        return resolveWithLevel(server, player, serverLevel);
    }
}

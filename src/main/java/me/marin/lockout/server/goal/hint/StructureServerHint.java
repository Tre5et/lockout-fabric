package me.marin.lockout.server.goal.hint;

import com.mojang.datafixers.util.Pair;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public class StructureServerHint extends PositionServerHint {
    private final List<ResourceKey<Structure>> acceptableStructures;

    public StructureServerHint(List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Structure>> acceptableStructures) {
        super(applicableLevels);
        this.acceptableStructures = acceptableStructures;
    }

    @Override
    protected GoalHintResult<BlockPos> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level) {
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(acceptableStructures.stream().map(level.registryAccess().lookupOrThrow(Registries.STRUCTURE)::getOrThrow).toList()),
                player.blockPosition(),
                10_000,
                false
        );

        if(found == null) {
            return GoalHintResult.error("Failed to find the biome.");
        }

        return GoalHintResult.result(found.getFirst());
    }
}

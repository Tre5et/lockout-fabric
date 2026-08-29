package me.marin.lockout.server.goal.hint;

import com.mojang.datafixers.util.Pair;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class BiomeServerHint extends PositionServerHint {
    private final List<ResourceKey<Biome>> acceptableBiomes;

    public BiomeServerHint(List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Biome>> acceptableBiomes) {
        super(applicableLevels);
        this.acceptableBiomes = acceptableBiomes;
    }

    @Override
    protected GoalHintResult<BlockPos> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level) {
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
                biomeRegistryEntry -> acceptableBiomes.stream().anyMatch(biomeRegistryEntry::is),
                player.blockPosition(),
                10_000,
                32,
                64
        );

        if(found == null) {
            return GoalHintResult.error("Failed to find the biome.");
        }

        return GoalHintResult.result(found.getFirst());
    }
}

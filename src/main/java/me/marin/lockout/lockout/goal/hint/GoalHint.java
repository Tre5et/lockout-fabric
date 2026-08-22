package me.marin.lockout.lockout.goal.hint;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

public abstract class GoalHint {
    @Getter
    private final String name;

    public GoalHint(String name) {
        this.name = name;
    }

    public abstract GoalHintResult resolve(MinecraftServer server, Player player);

    public static abstract class LevelBased extends GoalHint {
        private final List<ResourceKey<Level>> applicableLevels;

        public LevelBased(String name, List<ResourceKey<Level>> applicableLevels) {
            super(name);
            this.applicableLevels = applicableLevels;
        }

        protected abstract GoalHintResult resolveWithLevel(MinecraftServer server, Player player, ServerLevel level);

        @Override
        public GoalHintResult resolve(MinecraftServer server, Player player) {
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

    public static class Biomes extends LevelBased {
        private final List<ResourceKey<Biome>> biomes;

        public Biomes(String name, List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Biome>> biomes) {
            super("Nearest " + name, applicableLevels);
            this.biomes = biomes;
        }

        @Override
        public GoalHintResult resolveWithLevel(MinecraftServer server, Player player, ServerLevel level) {
            Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
                    biomeRegistryEntry -> biomes.stream().anyMatch(biomeRegistryEntry::is),
                    player.blockPosition(),
                    10_000,
                    32,
                    64
            );

            if(found == null) {
                return GoalHintResult.error("Failed to find the biome.");
            }

            return GoalHintResult.result(getName() + " at " + found.getFirst().toShortString());
        }
    }

    public static class Structures extends LevelBased {
        private final List<ResourceKey<Structure>> structures;

        public Structures(String name, List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Structure>> structures) {
            super("Nearest " + name, applicableLevels);
            this.structures = structures;
        }

        @Override
        public GoalHintResult resolveWithLevel(MinecraftServer server, Player player, ServerLevel level) {
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(
                    level,
                    HolderSet.direct(structures.stream().map(level.registryAccess().lookupOrThrow(Registries.STRUCTURE)::getOrThrow).toList()),
                    player.blockPosition(),
                    10_000,
                    false
            );

            if(found == null) {
                return GoalHintResult.error("Failed to find the biome.");
            }

            return GoalHintResult.result(getName() + " at " + found.getFirst().toShortString());
        }
    }
}

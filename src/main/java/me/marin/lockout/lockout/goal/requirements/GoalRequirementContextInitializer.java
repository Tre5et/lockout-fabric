package me.marin.lockout.lockout.goal.requirements;

import me.marin.lockout.LocateData;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface GoalRequirementContextInitializer {
    int SEARCH_RADIUS = 750;

    GoalRequirementContext initialize(MinecraftServer server, GoalRequirementContext current);

    default GoalRequirementContext initialize(MinecraftServer server) {
        return initialize(server, new GoalRequirementContext(Map.of(), Map.of(), List.of()));
    }

    class None implements GoalRequirementContextInitializer {
        @Override
        public GoalRequirementContext initialize(MinecraftServer server, GoalRequirementContext current) {
            return current;
        }
    }

    class Biomes implements GoalRequirementContextInitializer {
        private final List<ResourceKey<Biome>> biomes;

        public Biomes(List<ResourceKey<Biome>> biomes) {
            this.biomes = biomes;
        }

        @Override
        public GoalRequirementContext initialize(MinecraftServer server, GoalRequirementContext current) {
            return new GoalRequirementContext(
                    locateBiomes(server, current.biomes()),
                    current.structures(),
                    current.teams()
            );
        }

        public Map<ResourceKey<Biome>, LocateData> locateBiomes(MinecraftServer server, Map<ResourceKey<Biome>, LocateData> current) {
            Map<ResourceKey<Biome>, LocateData> locatedBiomes = new HashMap<>(current);

            for(ResourceKey<Biome> biome : biomes) {
                if (locatedBiomes.containsKey(biome)) continue;

                var spawnPoint = server.overworld().getRespawnData();
                var currentPos = spawnPoint.pos();

                var pair = server.overworld().findClosestBiome3d(
                        biomeRegistryEntry -> biomeRegistryEntry.is(biome),
                        currentPos,
                        SEARCH_RADIUS,
                        32,
                        64
                );

                LocateData data = new LocateData(false, 0);
                if (pair != null) {
                    int distance = Mth.floor(LocateCommand.dist(currentPos.getX(), currentPos.getZ(), pair.getFirst().getX(), pair.getFirst().getZ()));
                    if (distance < SEARCH_RADIUS) {
                        data = new LocateData(true, distance);
                    }
                }
                locatedBiomes.put(biome, data);

            }
            return locatedBiomes;
        }
    }

    class Structures implements GoalRequirementContextInitializer {
        private final List<ResourceKey<Structure>> structures;

        public Structures(List<ResourceKey<Structure>> structures) {
            this.structures = structures;
        }

        @Override
        public GoalRequirementContext initialize(MinecraftServer server, GoalRequirementContext current) {
            return new GoalRequirementContext(
                    current.biomes(),
                    locateStructures(server, current.structures()),
                    current.teams()
            );
        }

        public Map<ResourceKey<Structure>, LocateData> locateStructures(MinecraftServer server, Map<ResourceKey<Structure>, LocateData> current) {
            Map<ResourceKey<Structure>, LocateData> locatedStructures = new HashMap<>(current);

            for(ResourceKey<Structure> structure : structures) {
                if (locatedStructures.containsKey(structure)) continue;

                var spawnPoint = server.overworld().getRespawnData();
                var currentPos = spawnPoint.pos();

                var pair = server.overworld().getChunkSource().getGenerator().findNearestMapStructure(
                        server.overworld(),
                        HolderSet.direct(server.overworld().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)),
                        currentPos,
                        SEARCH_RADIUS,
                        false
                );


                LocateData data = new LocateData(false, 0);
                if (pair != null) {
                    int distance = Mth.floor(LocateCommand.dist(currentPos.getX(), currentPos.getZ(), pair.getFirst().getX(), pair.getFirst().getZ()));
                    if (distance < SEARCH_RADIUS) {
                        data = new LocateData(true, distance);
                    }
                }
                locatedStructures.put(structure, data);

            }
            return locatedStructures;
        }
    }

    class Combined implements GoalRequirementContextInitializer {
        private final List<GoalRequirementContextInitializer> initializers;

        public Combined(List<GoalRequirementContextInitializer> initializers) {
            this.initializers = initializers;
        }

        @Override
        public GoalRequirementContext initialize(MinecraftServer server, GoalRequirementContext current) {
            GoalRequirementContext context = current;
            for(GoalRequirementContextInitializer initializer : initializers) {
                context = initializer.initialize(server, context);
            }
            return context;
        }
    }
}

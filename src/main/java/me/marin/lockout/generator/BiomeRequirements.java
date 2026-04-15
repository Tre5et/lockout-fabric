package me.marin.lockout.generator;

import me.marin.lockout.LocateData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BiomeRequirements {

    public static BiomeRequirement single(ResourceKey<Biome> biome) {
        return new SingleBiomeRequirement(biome);
    }

    public static BiomeRequirement anyOf(List<ResourceKey<Biome>> biomes) {
        return new AnyOfBiomeRequirement(biomes);
    }

    @SafeVarargs
    public static BiomeRequirement anyOf(ResourceKey<Biome>... biomes) {
        return new AnyOfBiomeRequirement(Arrays.asList(biomes));
    }

    public static BiomeRequirement allOf(List<BiomeRequirement> requirements) {
        return new AllOfRequirement(requirements);
    }

    public static BiomeRequirement allOf(BiomeRequirement... requirements) {
        return new AllOfRequirement(Arrays.asList(requirements));
    }

    public static BiomeRequirement anyOfReq(List<BiomeRequirement> requirements) {
        return new AnyOfRequirement(requirements);
    }

    public static BiomeRequirement anyOfReq(BiomeRequirement... requirements) {
        return new AnyOfRequirement(Arrays.asList(requirements));
    }

    private static class SingleBiomeRequirement implements BiomeRequirement {
        private final ResourceKey<Biome> biome;

        public SingleBiomeRequirement(ResourceKey<Biome> biome) {
            this.biome = biome;
        }

        @Override
        public boolean isMet(Map<ResourceKey<Biome>, LocateData> biomes) {
            LocateData data = biomes.get(biome);
            return data != null && data.wasLocated();
        }

        @Override
        public void collectBiomes(java.util.Collection<ResourceKey<Biome>> collector) {
            collector.add(biome);
        }
    }

    private static class AnyOfBiomeRequirement implements BiomeRequirement {
        private final List<ResourceKey<Biome>> requiredBiomes;

        public AnyOfBiomeRequirement(List<ResourceKey<Biome>> requiredBiomes) {
            this.requiredBiomes = requiredBiomes;
        }

        @Override
        public boolean isMet(Map<ResourceKey<Biome>, LocateData> biomes) {
            return requiredBiomes.stream().anyMatch(biome -> {
                LocateData data = biomes.get(biome);
                return data != null && data.wasLocated();
            });
        }

        @Override
        public void collectBiomes(java.util.Collection<ResourceKey<Biome>> collector) {
            collector.addAll(requiredBiomes);
        }
    }

    private static class AllOfRequirement implements BiomeRequirement {
        private final List<BiomeRequirement> requirements;

        public AllOfRequirement(List<BiomeRequirement> requirements) {
            this.requirements = requirements;
        }

        @Override
        public boolean isMet(Map<ResourceKey<Biome>, LocateData> biomes) {
            return requirements.stream().allMatch(req -> req.isMet(biomes));
        }

        @Override
        public void collectBiomes(java.util.Collection<ResourceKey<Biome>> collector) {
            for (BiomeRequirement req : requirements) {
                req.collectBiomes(collector);
            }
        }
    }

    private static class AnyOfRequirement implements BiomeRequirement {
        private final List<BiomeRequirement> requirements;

        public AnyOfRequirement(List<BiomeRequirement> requirements) {
            this.requirements = requirements;
        }

        @Override
        public boolean isMet(Map<ResourceKey<Biome>, LocateData> biomes) {
            return requirements.stream().anyMatch(req -> req.isMet(biomes));
        }

        @Override
        public void collectBiomes(java.util.Collection<ResourceKey<Biome>> collector) {
            for (BiomeRequirement req : requirements) {
                req.collectBiomes(collector);
            }
        }
    }

}

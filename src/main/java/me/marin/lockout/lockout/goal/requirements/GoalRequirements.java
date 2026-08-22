package me.marin.lockout.lockout.goal.requirements;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Arrays;

public class GoalRequirements {
    @SafeVarargs
    public static GoalRequirement.AllBiomes allBiomes(ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AllBiomes(Arrays.stream(biomes).toList());
    }

    @SafeVarargs
    public static GoalRequirement.AnyBiome anyBiome(ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AnyBiome(Arrays.stream(biomes).toList());
    }

    @SafeVarargs
    public static GoalRequirement.AllStructures allStructures(ResourceKey<Structure>... biomes) {
        return new GoalRequirement.AllStructures(Arrays.stream(biomes).toList());
    }

    @SafeVarargs
    public static GoalRequirement.AnyStructure anyStructure(ResourceKey<Structure>... biomes) {
        return new GoalRequirement.AnyStructure(Arrays.stream(biomes).toList());
    }

    public static GoalRequirement.TeamCountMin minTeams(int count) {
        return new GoalRequirement.TeamCountMin(count);
    }

    public static GoalRequirement.TeamCountMax maxTeams(int count) {
        return new GoalRequirement.TeamCountMax(count);
    }

    public static GoalRequirement.AndCombined<Object> teamsBetween(int min, int max) {
        return minTeams(min).and(maxTeams(max));
    }

    public static final GoalRequirement.AnyStructure VILLAGE = anyStructure(
            BuiltinStructures.VILLAGE_PLAINS, BuiltinStructures.VILLAGE_DESERT, BuiltinStructures.VILLAGE_SAVANNA, BuiltinStructures.VILLAGE_SNOWY, BuiltinStructures.VILLAGE_TAIGA
    );

    public static final GoalRequirement.AnyBiome JUNGLE = anyBiome(
            Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SPARSE_JUNGLE
    );

    public static final GoalRequirement.AnyBiome DESERT_LIKE = anyBiome(
            Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS
    );

    public static final GoalRequirement.AnyBiome SNOWY = anyBiome(
            Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA, Biomes.GROVE, Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS,
            Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
    );

    public static final GoalRequirement.AndCombined<DyeColor> COLORS = DESERT_LIKE.forOptions(DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN)
            .and(JUNGLE.forOptions(DyeColor.BROWN));
}

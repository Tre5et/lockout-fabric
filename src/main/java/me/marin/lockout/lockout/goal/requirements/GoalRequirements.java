package me.marin.lockout.lockout.goal.requirements;

import me.marin.lockout.client.goal.hint.PositionClientHint;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.server.goal.hint.BiomeServerHint;
import me.marin.lockout.server.goal.hint.StructureServerHint;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Arrays;
import java.util.List;

public class GoalRequirements {
    @SafeVarargs
    public static GoalRequirement.AllBiomes allBiomes(ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AllBiomes(Arrays.stream(biomes).toList());
    }

    @SafeVarargs
    public static GoalRequirement<Object> biome(String name, ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AnyBiome(Arrays.stream(biomes).toList())
                .withHint(new HintCombination<>(
                        () -> new BiomeServerHint(List.of(Level.OVERWORLD), Arrays.stream(biomes).toList()),
                        () -> new PositionClientHint(name)
                ));
    }

    public static GoalRequirement<Object> biome(ResourceKey<Biome> biome) {
        return biome(biome.identifier().getPath(), biome);
    }

    @SafeVarargs
    public static GoalRequirement.AllStructures allStructures(ResourceKey<Structure>... structures) {
        return new GoalRequirement.AllStructures(Arrays.stream(structures).toList());
    }

    @SafeVarargs
    public static GoalRequirement<Object> structure(String name, ResourceKey<Structure>... structures) {
        return new GoalRequirement.AnyStructure(Arrays.stream(structures).toList())
                .withHint(new HintCombination<>(
                        () -> new StructureServerHint(List.of(Level.OVERWORLD), Arrays.stream(structures).toList()),
                        () -> new PositionClientHint(name)
                ));
    }

    public static GoalRequirement<Object> structure(ResourceKey<Structure> structure) {
        return structure(structure.identifier().getPath(), structure);
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

    public static final GoalRequirement<Object> VILLAGE = structure(
            "Village", BuiltinStructures.VILLAGE_PLAINS, BuiltinStructures.VILLAGE_DESERT, BuiltinStructures.VILLAGE_SAVANNA, BuiltinStructures.VILLAGE_SNOWY, BuiltinStructures.VILLAGE_TAIGA
    );

    public static final GoalRequirement<Object> JUNGLE = biome(
            "Jungle", Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SPARSE_JUNGLE
    );

    public static final GoalRequirement<Object> DESERT_LIKE = biome(
            "Desert-like Biome", Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS
    );

    public static final GoalRequirement<Object> TAIGA = biome("Taiga",
            Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.SNOWY_TAIGA, Biomes.GROVE
    );

    public static final GoalRequirement<Object> SWAMP = biome("Swamp",
            Biomes.SWAMP, Biomes.MANGROVE_SWAMP
    );

    public static final GoalRequirement<Object> SNOWY_MOUNTAINS = biome("Snowy Mountains",
            Biomes.JAGGED_PEAKS, Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES
    );

    public static final GoalRequirement<Object> SNOWY = biome(
            "Snowy Biome", Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA, Biomes.GROVE, Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS,
            Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
    );

    public static final GoalRequirement<Object> WARM_OCEAN = biome("Warm Ocean",
            Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN
    );

    public static final GoalRequirement.AndCombined<DyeColor> COLORS = DESERT_LIKE.forOptions(DyeColor.GREEN, DyeColor.CYAN)
            .and(JUNGLE.forOptions(DyeColor.BROWN))
            .and(DESERT_LIKE.or(biome("Sea-pickle Biome", Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN)).forOptions(DyeColor.LIME));
}

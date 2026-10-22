package me.marin.lockout.lockout.goal.requirements;

import me.marin.lockout.client.goal.hint.PositionClientHint;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.server.goal.hint.BiomeServerHint;
import me.marin.lockout.server.goal.hint.StructureServerHint;
import me.marin.lockout.util.LockoutMapDecorationTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

import java.util.Arrays;
import java.util.List;

public class GoalRequirements {
    @SafeVarargs
    public static GoalRequirement.AllBiomes allBiomes(ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AllBiomes(Arrays.stream(biomes).toList());
    }

    @SafeVarargs
    public static GoalRequirement<Object> biome(String name, Holder<MapDecorationType> decoration, ResourceKey<Biome>... biomes) {
        return new GoalRequirement.AnyBiome(Arrays.stream(biomes).toList())
                .withHint(new HintCombination<>(
                        () -> new BiomeServerHint(name, decoration, List.of(Level.OVERWORLD), Arrays.stream(biomes).toList()),
                        () -> new PositionClientHint(name)
                ));
    }

    public static GoalRequirement<Object> biome(ResourceKey<Biome> biome, Holder<MapDecorationType> decoration) {
        return biome(biome.identifier().getPath(), decoration, biome);
    }

    @SafeVarargs
    public static GoalRequirement.AllStructures allStructures(ResourceKey<Structure>... structures) {
        return new GoalRequirement.AllStructures(Arrays.stream(structures).toList());
    }

    @SafeVarargs
    public static GoalRequirement<Object> structure(String name, Holder<MapDecorationType> decoration, ResourceKey<Structure>... structures) {
        return new GoalRequirement.AnyStructure(Arrays.stream(structures).toList())
                .withHint(new HintCombination<>(
                        () -> new StructureServerHint(name, decoration, List.of(Level.OVERWORLD), Arrays.stream(structures).toList()),
                        () -> new PositionClientHint(name)
                ));
    }

    public static GoalRequirement<Object> structure(ResourceKey<Structure> structure, Holder<MapDecorationType> decoration) {
        return structure(structure.identifier().getPath(), decoration, structure);
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
            "Village", MapDecorationTypes.PLAINS_VILLAGE, BuiltinStructures.VILLAGE_PLAINS, BuiltinStructures.VILLAGE_DESERT, BuiltinStructures.VILLAGE_SAVANNA, BuiltinStructures.VILLAGE_SNOWY, BuiltinStructures.VILLAGE_TAIGA
    );

    public static final GoalRequirement<Object> JUNGLE = biome(
            "Jungle", LockoutMapDecorationTypes.JUNGLE, Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SPARSE_JUNGLE
    );

    public static final GoalRequirement<Object> DESERT_LIKE = biome(
            "Desert-like Biome", LockoutMapDecorationTypes.DESERT, Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS
    );

    public static final GoalRequirement<Object> TAIGA = biome("Taiga", LockoutMapDecorationTypes.TAIGA,
            Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.SNOWY_TAIGA, Biomes.GROVE
    );

    public static final GoalRequirement<Object> SWAMP = biome("Swamp", LockoutMapDecorationTypes.SWAMP,
            Biomes.SWAMP, Biomes.MANGROVE_SWAMP
    );

    public static final GoalRequirement<Object> SNOWY_MOUNTAINS = biome("Snowy Mountains", MapDecorationTypes.WHITE_BANNER,
            Biomes.JAGGED_PEAKS, Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES
    );

    public static final GoalRequirement<Object> SNOWY = biome(
            "Snowy Biome", MapDecorationTypes.WHITE_BANNER, Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA, Biomes.GROVE, Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS,
            Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
    );

    public static final GoalRequirement<Object> POLAR = biome("Polar Biome", MapDecorationTypes.WHITE_BANNER,
            Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SNOWY_PLAINS
    );

    public static final GoalRequirement<Object> OCEAN = biome("Ocean", MapDecorationTypes.BLUE_BANNER,
            Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN,
            Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN
    );

    public static final GoalRequirement<Object> WARM_OCEAN = biome("Warm Ocean", MapDecorationTypes.CYAN_BANNER,
            Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN
    );

    public static final GoalRequirement<Object> NAUSEA_BIOME = biome("Nausea Biome", MapDecorationTypes.BLUE_BANNER,
            Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.PALE_GARDEN, Biomes.SULFUR_CAVES
    );

    public static final GoalRequirement<Object> ALLAY_STRUCTURE = structure("Allay Structure", MapDecorationTypes.LIGHT_BLUE_BANNER,
            BuiltinStructures.PILLAGER_OUTPOST, BuiltinStructures.WOODLAND_MANSION
    );
    public static final GoalRequirement<Object> SAVANNA = biome("Savanna", MapDecorationTypes.ORANGE_BANNER,
            Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU
    );
    public static final GoalRequirement<Object> DESERT = biome("Desert", MapDecorationTypes.YELLOW_BANNER, Biomes.DESERT);
    public static final GoalRequirement<Object> DESERT_VILLAGE = structure("Desert Village", MapDecorationTypes.DESERT_VILLAGE,
            BuiltinStructures.VILLAGE_DESERT
    );
    public static final GoalRequirement<Object> SPIKY_CAVES = biome("Spiky Caves", MapDecorationTypes.BROWN_BANNER,
            Biomes.DRIPSTONE_CAVES, Biomes.SULFUR_CAVES
    );
    public static final GoalRequirement<Object> DEEP_DARK = biome("Deep Dark", MapDecorationTypes.ANCIENT_CITY, Biomes.DEEP_DARK);
    public static final GoalRequirement<Object> LUSH_CAVES = biome("Lush Caves", MapDecorationTypes.GREEN_BANNER, Biomes.LUSH_CAVES);
    public static final GoalRequirement<Object> PALE_GARDEN = biome("Pale Garden", MapDecorationTypes.LIGHT_GRAY_BANNER, Biomes.PALE_GARDEN);
    public static final GoalRequirement<Object> SULFUR_CAVES = biome("Sulfur Caves", LockoutMapDecorationTypes.SULFUR_CAVE, Biomes.SULFUR_CAVES);
    public static final GoalRequirement<Object> DEAD_BIOME = biome("Dead Biome", MapDecorationTypes.ORANGE_BANNER,
            Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS, Biomes.SWAMP, Biomes.MANGROVE_SWAMP,
            Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA
    );
    public static final GoalRequirement<Object> MANGROVE_SWAMP = biome("Mangrove Swamp", MapDecorationTypes.GREEN_BANNER,
            Biomes.MANGROVE_SWAMP
    );
    public static final GoalRequirement<Object> DAPPLED_FOREST = biome("Dappled Forest", MapDecorationTypes.ORANGE_BANNER,
            Biomes.DAPPLED_FOREST
    );
    public static final GoalRequirement<Object> BEACH = biome(Biomes.BEACH, MapDecorationTypes.YELLOW_BANNER);
    public static final GoalRequirement<Object> HORSE_BIOME = biome("Horse Biome", MapDecorationTypes.GREEN_BANNER,
            Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA
    );
    public static final GoalRequirement<Object> WOLF_BIOME = biome("Wolf Biome", MapDecorationTypes.WHITE_BANNER,
            Biomes.FOREST, Biomes.GROVE, Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.SPARSE_JUNGLE, Biomes.SAVANNA_PLATEAU, Biomes.WOODED_BADLANDS
    );
    public static final GoalRequirement<Object> PILLAGER_OUTPOST = structure("Pillager Outpost", MapDecorationTypes.GRAY_BANNER,
            BuiltinStructures.PILLAGER_OUTPOST
    );
    public static final GoalRequirement<Object> TRIAL_CHAMBERS = structure("Trial Chambers", MapDecorationTypes.TRIAL_CHAMBERS,
            BuiltinStructures.TRIAL_CHAMBERS
    );
    public static final GoalRequirement<Object> OCEAN_MONUMENT = structure("Ocean Monument", MapDecorationTypes.OCEAN_MONUMENT,
            BuiltinStructures.OCEAN_MONUMENT
    );
    public static final GoalRequirement<Object> ANCIENT_CITY = structure("Ancient City", MapDecorationTypes.ANCIENT_CITY,
            BuiltinStructures.ANCIENT_CITY
    );
    public static final GoalRequirement<Object> BAD_OMEN_STRUCTURE = structure("Bad Omen Structure", MapDecorationTypes.RED_BANNER,
            BuiltinStructures.PILLAGER_OUTPOST, BuiltinStructures.TRIAL_CHAMBERS
    );
    public static final GoalRequirement<Object> BURIED_TREASURE = structure("Buried Treasure", MapDecorationTypes.RED_X,
            BuiltinStructures.BURIED_TREASURE
    );
    public static final GoalRequirement<Object> SWAMP_HUT = structure("Swamp Hut", MapDecorationTypes.SWAMP_HUT,
            BuiltinStructures.SWAMP_HUT
    );
    public static final GoalRequirement<Object> SPAWNER_STRUCTURE = structure("Spawner Structure", MapDecorationTypes.GRAY_BANNER,
            BuiltinStructures.MINESHAFT, BuiltinStructures.MINESHAFT_MESA, BuiltinStructures.STRONGHOLD, BuiltinStructures.TRIAL_CHAMBERS
    );

    public static final GoalRequirement<Object> SUSPICIOUS = structure("Suspicious Structure", MapDecorationTypes.PURPLE_BANNER,
            BuiltinStructures.TRAIL_RUINS, BuiltinStructures.OCEAN_RUIN_WARM, BuiltinStructures.OCEAN_RUIN_COLD, BuiltinStructures.DESERT_PYRAMID
    );

    public static final GoalRequirement.AndCombined<DyeColor> COLORS = DESERT_LIKE.forOptions(DyeColor.GREEN, DyeColor.CYAN)
            .and(JUNGLE.forOptions(DyeColor.BROWN))
            .and(DESERT_LIKE.or(biome("Sea-pickle Biome", MapDecorationTypes.CYAN_BANNER, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN).forOptions(DyeColor.LIME))
    );
}

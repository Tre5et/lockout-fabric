package me.marin.lockout.lockout;

import me.marin.lockout.lockout.goal.builder.entity.BreedUniqueAnimalsGoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.SpawnEntityGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.BreakItemGoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.BreedAnimalGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.BrewPotionGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.BrewPotionTypeGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.CompostUniqueItemsGoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

import static me.marin.lockout.lockout.GoalRegistry.INSTANCE;

public class DefaultGoalRegister {
    private static boolean goalsRegistered = false;

    public static synchronized void registerGoals() {
        if (goalsRegistered) {
            return;
        }

        INSTANCE.register(BreakItemGoalBuilder.withComponent("ANY_ARMOR_PIECE", GoalCategory.ARMOR, DataComponents.EQUIPPABLE)
                .customName(_ -> "Break any armor piece")
                .group(GoalGroups.ARMOR_SPECIAL));
        INSTANCE.register(BreakItemGoalBuilder.withComponent("ANY_TOOL", GoalCategory.TOOLS, DataComponents.TOOL)
                .customName(_ -> "Break any tool")
                .group(GoalGroups.TOOLS));

        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.ARMADILLO).require(GoalRequirements.anyBiome("Savanna", Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU)));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.CAMEL).require(GoalRequirements.anyBiome("Desert", Biomes.DESERT).or(GoalRequirements.anyStructure("Desert Village", BuiltinStructures.VILLAGE_DESERT))));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.CHICKEN));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.COW, EntityTypes.MOOSHROOM).customName(_ -> "Breed Cow"));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.FOX).require(GoalRequirements.TAIGA));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.FROG).require(GoalRequirements.SWAMP));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.GOAT).require(GoalRequirements.SNOWY_MOUNTAINS));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.HOGLIN));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.PIG));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.RABBIT));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.SHEEP));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.STRIDER));
        INSTANCE.register(BreedUniqueAnimalsGoalBuilder.range(4,8));

        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.HEALING, Potions.STRONG_HEALING));
        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.INVISIBILITY, Potions.LONG_INVISIBILITY));
        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON));
        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS));
        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.WATER_BREATHING, Potions.LONG_WATER_BREATHING));
        INSTANCE.register(BrewPotionGoalBuilder.any(Potions.WEAKNESS, Potions.LONG_WEAKNESS));
        INSTANCE.register(BrewPotionTypeGoalBuilder.of(Items.LINGERING_POTION));

        INSTANCE.register(CompostUniqueItemsGoalBuilder.any("FOODS", 3, 7, Items.DRIED_KELP, Items.GLOW_BERRIES, Items.SWEET_BERRIES, Items.MELON_SLICE, Items.APPLE, Items.BEETROOT, Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.BREAD, Items.COOKIE, Items.CAKE, Items.PUMPKIN_PIE)
                .customName(n -> "Compost " + n + " Unique Foods")
        );

        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.IRON_GOLEM).group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.COPPER_GOLEM));
        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.SNOW_GOLEM).require(GoalRequirements.SNOWY));

/*        INSTANCE.register(ObtainAllItemGoalBuilder.simple("ALL_WOODEN_TOOLS", GoalCategory.TOOLS, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_SPEAR)
                .customName(_ -> "Obtain all Wooden Tools"));
        INSTANCE.register(ObtainColoredItemGoalBuilder.withCount("64_WOOL", GoalCategory.OBTAINING_ITEMS, Items.WOOL, 64));
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.WITHER_SKELETON_SKULL)
                .defaultEnabled(false)
        );
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.SULFUR_CUBE_BUCKET)
                .require(GoalRequirements.anyBiome("Sulfur Caves", SULFUR_CAVES))
        );
        INSTANCE.register(RideEntityGoalBuilder.simple(GoalCategory.RIDING, EntityTypes.HORSE));
        INSTANCE.register(RideEntityGoalBuilder.simple(GoalCategory.RIDING, EntityTypes.STRIDER)
                .customName(_ -> "Ride a Strider with custom name")
        );
        INSTANCE.register(ObtainSomeItemsGoalBuilder.multiple("4_UNIQUE_SEEDS", 4, GoalCategory.OBTAINING_ITEMS, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS)
                .customName(_ -> "Obtain 4 unique Seeds")
        );
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.BELL)
                .require(GoalRequirements.VILLAGE)
        );
        INSTANCE.register(ObtainAllItemGoalBuilder.withCounts(GoalCategory.OBTAINING_ITEMS, new Pair<>(Items.IRON_INGOT, 24))
                .group(GoalGroups.IRON_HEAVY)
        );
        INSTANCE.register(ObtainColoredItemGoalBuilder.simple("COLORED_CONCRETE", GoalCategory.OBTAINING_ITEMS, Items.CONCRETE));
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.COD, Items.SALMON));
        INSTANCE.register(AdvancementGoalBuilder.any(GoalCategory.ADVANCEMENTS, Identifier.withDefaultNamespace("adventure/bullseye")));
        INSTANCE.register(AdvancementGoalBuilder.any("ANY_SPYGLASS", GoalCategory.ADVANCEMENTS, Identifier.withDefaultNamespace("adventure/spyglass_at_parrot"), Identifier.withDefaultNamespace("adventure/spyglass_at_ghast"), Identifier.withDefaultNamespace("adventure/spyglass_at_dragon"))
                .customName(_ -> "Obtain any Spyglass Advancement")
        );
        INSTANCE.register(AdvancementGoalBuilder.counting("UNIQUE_ADVANCEMENTS", GoalCategory.ADVANCEMENTS, 3, 30));*/

        goalsRegistered = true;
    }

}

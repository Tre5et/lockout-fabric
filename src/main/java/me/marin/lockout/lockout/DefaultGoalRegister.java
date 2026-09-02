package me.marin.lockout.lockout;

import me.marin.lockout.lockout.goal.builder.damage.DealDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DeathGoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.SpawnEntityGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.*;
import me.marin.lockout.lockout.goal.builder.entity.BreedAnimalGoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.SpriteTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import oshi.util.tuples.Pair;

import java.util.List;

import static me.marin.lockout.lockout.GoalRegistry.INSTANCE;

public class DefaultGoalRegister {
    private static boolean goalsRegistered = false;

    public static synchronized void registerGoals() {
        if (goalsRegistered) {
            return;
        }

        INSTANCE.register(BreakItemGoalBuilder.withComponent(GoalCategory.ARMOR, DataComponents.EQUIPPABLE)
                .customName(_ -> "Break any armor piece")
                .group(GoalGroups.ARMOR_SPECIAL));
        INSTANCE.register(BreakItemGoalBuilder.withComponent(GoalCategory.TOOLS, DataComponents.TOOL)
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
        INSTANCE.register(BreedAnimalGoalBuilder.unique(4,8));

        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.HEALING, Potions.STRONG_HEALING));
        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.INVISIBILITY, Potions.LONG_INVISIBILITY));
        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON));
        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS));
        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.WATER_BREATHING, Potions.LONG_WATER_BREATHING));
        INSTANCE.register(BrewPotionGoalBuilder.effect(Potions.WEAKNESS, Potions.LONG_WEAKNESS));
        INSTANCE.register(BrewPotionGoalBuilder.type(Items.LINGERING_POTION).customName(_ -> "Brew any Lingering Potion"));

        INSTANCE.register(CompostItemsGoalBuilder.uniqueWithComponent(3, 7, DataComponents.FOOD)
                .customName(n -> "Compost " + n + " Unique Foods")
        );

        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.IRON_GOLEM).group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.COPPER_GOLEM));
        INSTANCE.register(SpawnEntityGoalBuilder.any(EntityTypes.SNOW_GOLEM).require(GoalRequirements.SNOWY));

        INSTANCE.register(CraftItemGoalBuilder.unique(20, 100, 10));

        INSTANCE.register(DealDamageGoalBuilder.total(100, 500, 25));

        INSTANCE.register(ObtainItemGoalBuilder.shieldWithBanner().customName(_ -> "Obtain Shield with Banner"));

        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.DROWN, () -> new StackingTextureExtractor(List.of(SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_empty")), SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_bursting"))), 0))
                .customName(_ -> "Die by Drowning"));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.FELL_OUT_OF_WORLD, () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/spark_4.png")))
                .customName(_ -> "Die by Falling in Void"));
        INSTANCE.register(DeathGoalBuilder.fallLocation(new Pair<>(FallLocation.VINES, Items.VINE), new Pair<>(FallLocation.TWISTING_VINES, Items.TWISTING_VINES), new Pair<>(FallLocation.WEEPING_VINES, Items.WEEPING_VINES))
                .customName(_ -> "Die by Falling of Vines"));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.FREEZE, () -> SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/heart/frozen_full")))
                .customName(_ -> "Die by Freezing")
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.MAGIC, () -> new ItemTextureExtractor(ItemUtil.applyComponent(Items.POTION.getDefaultInstance(), DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING))))
                .customName(_ -> "Die by Magic"));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.BAD_RESPAWN_POINT, () -> ItemTextureExtractor.cycleItems(List.of(Items.BED.red(), Items.RESPAWN_ANCHOR)))
                .customName(_ -> "Die to [Intentional Game Design]"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.BEE));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.SWEET_BERRY_BUSH, () -> ItemTextureExtractor.item(Items.SWEET_BERRIES))
                .customName(_ -> "Die to Berry Bush")
                .require(GoalRequirements.TAIGA));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.CACTUS, () -> ItemTextureExtractor.item(Items.CACTUS))
                .customName(_ -> "Die to Cactus")
                .require(GoalRequirements.DESERT_LIKE));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.FALLING_ANVIL, () -> ItemTextureExtractor.item(Items.ANVIL))
                .customName(_ -> "Die to falling Anvil")
                .group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.FALLING_STALACTITE, () -> ItemTextureExtractor.cycleItems(List.of(Items.POINTED_DRIPSTONE, Items.SULFUR_SPIKE)))
                .customName(_ -> "Die to falling Stalactite")
                .require(GoalRequirements.anyBiome("Spiky Caves", Biomes.DRIPSTONE_CAVES, Biomes.SULFUR_CAVES)));
        INSTANCE.register(DeathGoalBuilder.type(DamageTypes.FIREWORKS, () -> ItemTextureExtractor.item(Items.FIREWORK_ROCKET))
                .customName(_ -> "Die to Firework Rocket"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.IRON_GOLEM).require(GoalRequirements.VILLAGE));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.POLAR_BEAR).require(GoalRequirements.SNOWY));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.PUFFERFISH).require(GoalRequirements.WARM_OCEAN));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.TNT_MINECART).customName(_ -> "Die to TNT Minecart"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.WARDEN).require(GoalRequirements.anyBiome("Deep Dark", Biomes.DEEP_DARK)));

        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.HONEY_BOTTLE).customName(_ -> "Drink Honey Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.anyWithComponent(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER), "WATER_BOTTLE", "Water Bottle", Items.POTION)
                .customName(_ -> "Drink Water Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.uniqueWithComponent(5, 25, DataComponents.FOOD)
                .customName(n -> "Eat " + n + " Unique Foods"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.GLOW_BERRIES).require(GoalRequirements.anyBiome("Lush Caves", Biomes.LUSH_CAVES))
                .customName(_ -> "Eat a Glow Berry"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.POISONOUS_POTATO).customName(_ -> "Eat a Poisonous Potato"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.COOKIE).require(GoalRequirements.JUNGLE).customName(_ -> "Eat a Cookie"));
        INSTANCE.register(ConsumeItemGoalBuilder.unique(2, 4, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.MUSHROOM_STEW, Items.SUSPICIOUS_STEW)
                .customName(n -> "Eat " + n + " Unique Soups"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.BEETROOT_SOUP).customName(_ -> "Eat Beetroot Soup"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.PUMPKIN_PIE).customName(_ -> "Eat Pumpkin Pie"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.RABBIT_STEW).customName(_ -> "Eat Rabbit Stew"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.SUSPICIOUS_STEW).customName(_ -> "Eat Suspicious Stew"));

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

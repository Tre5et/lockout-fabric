package me.marin.lockout.lockout;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.PlayerStateGoalBuilder;
import me.marin.lockout.lockout.goal.builder.advancement.ObtainAdvancementGoalBuilder;
import me.marin.lockout.lockout.goal.builder.block.UseItemOnBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.block.MineBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DealDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DeathGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.KillEntityGoal;
import me.marin.lockout.lockout.goal.builder.entity.*;
import me.marin.lockout.lockout.goal.builder.experience.ReachExperienceLevelGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.*;
import me.marin.lockout.lockout.goal.builder.statistic.ChangeStatisticGoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
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

        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.ARMADILLO).require(GoalRequirements.biome("Savanna", Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU)));
        INSTANCE.register(BreedAnimalGoalBuilder.any(EntityTypes.CAMEL).require(GoalRequirements.biome("Desert", Biomes.DESERT).or(GoalRequirements.structure("Desert Village", BuiltinStructures.VILLAGE_DESERT))));
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

        INSTANCE.register(ObtainItemGoalBuilder.shieldWithBanner());

        INSTANCE.register(DeathGoalBuilder.type(() -> new StackingTextureExtractor(List.of(SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_empty")), SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_bursting"))), 0), DamageTypes.DROWN)
                .customName(_ -> "Die by Drowning"));
        INSTANCE.register(DeathGoalBuilder.type(() -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/spark_4.png")), DamageTypes.FELL_OUT_OF_WORLD)
                .customName(_ -> "Die by Falling in Void"));
        INSTANCE.register(DeathGoalBuilder.fallLocation(new Pair<>(FallLocation.VINES, Items.VINE), new Pair<>(FallLocation.TWISTING_VINES, Items.TWISTING_VINES), new Pair<>(FallLocation.WEEPING_VINES, Items.WEEPING_VINES))
                .customName(_ -> "Die by Falling of Vines"));
        INSTANCE.register(DeathGoalBuilder.type(() -> SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/heart/frozen_full")), DamageTypes.FREEZE)
                .customName(_ -> "Die by Freezing")
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(DeathGoalBuilder.type(() -> new ItemTextureExtractor(ItemUtil.applyComponent(Items.POTION.getDefaultInstance(), DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING))), DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC)
                .customName(_ -> "Die by Magic"));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.cycleItems(List.of(Items.BED.red(), Items.RESPAWN_ANCHOR)), DamageTypes.BAD_RESPAWN_POINT)
                .customName(_ -> "Die to [Intentional Game Design]"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.BEE));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.item(Items.SWEET_BERRIES), DamageTypes.SWEET_BERRY_BUSH)
                .customName(_ -> "Die to Berry Bush")
                .require(GoalRequirements.TAIGA));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.item(Items.CACTUS), DamageTypes.CACTUS)
                .customName(_ -> "Die to Cactus")
                .require(GoalRequirements.DESERT_LIKE));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.item(Items.ANVIL), DamageTypes.FALLING_ANVIL)
                .customName(_ -> "Die to falling Anvil")
                .group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.cycleItems(List.of(Items.POINTED_DRIPSTONE, Items.SULFUR_SPIKE)), DamageTypes.FALLING_STALACTITE)
                .customName(_ -> "Die to falling Stalactite")
                .require(GoalRequirements.biome("Spiky Caves", Biomes.DRIPSTONE_CAVES, Biomes.SULFUR_CAVES)));
        INSTANCE.register(DeathGoalBuilder.type(() -> ItemTextureExtractor.item(Items.FIREWORK_ROCKET), DamageTypes.FIREWORKS)
                .customName(_ -> "Die to Firework Rocket"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.IRON_GOLEM).require(GoalRequirements.VILLAGE));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.POLAR_BEAR).require(GoalRequirements.SNOWY));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.PUFFERFISH).require(GoalRequirements.WARM_OCEAN));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.TNT_MINECART).customName(_ -> "Die to TNT Minecart"));
        INSTANCE.register(DeathGoalBuilder.entity(EntityTypes.WARDEN).require(GoalRequirements.biome("Deep Dark", Biomes.DEEP_DARK)));

        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.HONEY_BOTTLE).customName(_ -> "Drink Honey Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.anyWithComponent(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER), "WATER_BOTTLE", "Water Bottle", Items.POTION)
                .customName(_ -> "Drink Water Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.uniqueWithComponent(5, 25, DataComponents.FOOD)
                .customName(n -> "Eat " + n + " Unique Foods"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.GLOW_BERRIES).require(GoalRequirements.biome("Lush Caves", Biomes.LUSH_CAVES))
                .customName(_ -> "Eat a Glow Berry"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.POISONOUS_POTATO).customName(_ -> "Eat a Poisonous Potato"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.COOKIE).require(GoalRequirements.JUNGLE).customName(_ -> "Eat a Cookie"));
        INSTANCE.register(ConsumeItemGoalBuilder.unique(2, 4, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.MUSHROOM_STEW, Items.SUSPICIOUS_STEW)
                .customName(n -> "Eat " + n + " Unique Soups"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.BEETROOT_SOUP).customName(_ -> "Eat Beetroot Soup"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.PUMPKIN_PIE).customName(_ -> "Eat Pumpkin Pie"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.RABBIT_STEW).customName(_ -> "Eat Rabbit Stew"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(Items.SUSPICIOUS_STEW).customName(_ -> "Eat Suspicious Stew"));

        INSTANCE.register(PlayerStateGoalBuilder.emptyHungerBar());
        INSTANCE.register(PlayerStateGoalBuilder.heightAbove(320, ServerLevel.OVERWORLD).customName(_ -> "Reach height limit"));
        INSTANCE.register(PlayerStateGoalBuilder.heightAbove(128, ServerLevel.NETHER).customName(_ -> "Reach Nether Roof")
                .customTextureExtractor(_ -> new CornerIconTextureExtractor(
                        ItemTextureExtractor.item(Items.BEDROCK),
                        GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/up_arrow.png")),
                8))
                .defaultEnabled(false));

        INSTANCE.register(AngerMobGoalBuilder.any(EntityTypes.ZOMBIFIED_PIGLIN));

        INSTANCE.register(ObtainAdvancementGoalBuilder.any("story/enter_the_nether").customName(_ -> "Enter The Nether"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("story/enter_the_end").customName(_ -> "Enter The End"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("story/follow_ender_eye").customName(_ -> "Enter a Stronghold"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("nether/charge_respawn_anchor").customName(_ -> "Fully charge a Respawn Anchor"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/whos_the_pillager_now").customName(_ -> "Kill a Pillager using a Crossbow")
                .require(GoalRequirements.structure("Pillager Outpost", BuiltinStructures.PILLAGER_OUTPOST)));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/bullseye"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/spear_many_mobs"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("nether/distract_piglin"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/sniper_duel"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("husbandry/place_dried_ghast_in_water"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("nether/ride_strider"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("husbandry/wax_on"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("husbandry/wax_off"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.unique(5, 30, 1));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/spyglass_at_parrot", "adventure/spyglass_at_ghast", "adventure/spyglass_at_dragon").customName(_ -> "Obtain any Spyglass Advancement"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("adventure/trade").customName(_ -> "Trade with Villager"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("nether/brew_potion").customName(_ -> "Brew a Potion"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("story/enchant_item").customName(_ -> "Enchant Item at Enchanting Table"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("husbandry/make_a_sign_glow").customName(_ -> "Use Glow Ink on a Sign"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any("nether/explore_nether"));

        INSTANCE.register(KillEntityGoal.any(EntityTypes.END_CRYSTAL).customName(_ -> "Explode End Crystal").customTextureExtractor(_ -> ItemTextureExtractor.item(Items.END_CRYSTAL)));

        INSTANCE.register(KillEntityGoal.unique(5,15, EntityUtil.HOSTILE.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Unique Hostile Mobs"));
        INSTANCE.register(KillEntityGoal.total(10, 30, 1, EntityUtil.ARTHROPODS.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Arthropods"));
        INSTANCE.register(KillEntityGoal.total(10, 30, 1, EntityUtil.UNDEAD.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Undead Mobs"));
        INSTANCE.register(KillEntityGoal.unique(3,6, EntityUtil.RAID.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Raid Mobs"));
        INSTANCE.register(KillEntityGoal.total(50,150, 5, EntityUtil.HOSTILE.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Hostile Mobs"));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.BAT));
        INSTANCE.register(KillEntityGoal.coloredSheep());
        INSTANCE.register(KillEntityGoal.deathType(DamageTypes.WIND_CHARGE, () -> ItemTextureExtractor.item(Items.WIND_CHARGE), EntityTypes.BREEZE)
                .require(GoalRequirements.structure("Trial Chambers", BuiltinStructures.TRIAL_CHAMBERS)));
        INSTANCE.register(KillEntityGoal.deathType(DamageTypes.THROWN, () -> ItemTextureExtractor.item(Items.SNOWBALL), EntityTypes.BLAZE)
                .require(GoalRequirements.SNOWY).customName(_ -> "Kill Blaze using Snowball").defaultEnabled(false));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.BOGGED)
                .require(GoalRequirements.biome("Swamp", Biomes.SWAMP, Biomes.MANGROVE_SWAMP).or(GoalRequirements.structure("Trial Chambers", BuiltinStructures.TRIAL_CHAMBERS))));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.ELDER_GUARDIAN)
                .require(GoalRequirements.structure("Ocean Monument", BuiltinStructures.OCEAN_MONUMENT)));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.GUARDIAN)
                .require(GoalRequirements.structure("Ocean Monument", BuiltinStructures.OCEAN_MONUMENT)));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.HUSK)
                .require(GoalRequirements.biome("Desert", Biomes.DESERT)));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.SILVERFISH));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.SNOW_GOLEM)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoal.dimension(ServerLevel.NETHER, () -> ItemTextureExtractor.item(Items.NETHERRACK), EntityTypes.SNOW_GOLEM)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.STRAY)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.WARDEN)
                .require(GoalRequirements.structure("Ancient City", BuiltinStructures.ANCIENT_CITY)));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.WITCH));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.ZOGLIN));
        INSTANCE.register(KillEntityGoal.any(EntityTypes.ZOMBIE_VILLAGER));

        INSTANCE.register(ObtainItemGoalBuilder.colored(Items.CONCRETE, 64, "CONCRETE"));
        INSTANCE.register(ObtainItemGoalBuilder.colored(Items.WOOL, 64, "WOOL"));
        INSTANCE.register(ObtainItemGoalBuilder.withCount(Items.COARSE_DIRT, 64));
        INSTANCE.register(ObtainItemGoalBuilder.anyFullStack());
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.ACTIVATOR_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE, Items.GOLDEN_SPEAR).customName(_ -> "Obtain all Gold Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SPEAR).customName(_ -> "Obtain all Iron Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SPEAR).customName(_ -> "Obtain all Stone Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SPEAR).customName(_ -> "Obtain all Wooden Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.COPPER_SWORD, Items.COPPER_AXE, Items.COPPER_PICKAXE, Items.COPPER_SHOVEL, Items.COPPER_HOE, Items.COPPER_SPEAR).customName(_ -> "Obtain all Copper Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SPEAR).customName(_ -> "Obtain all Diamond Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.BELL));
        INSTANCE.register(ObtainItemGoalBuilder.colored(Items.GLAZED_TERRACOTTA, 1, "GLAZED_TERRACOTTA"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.RESIN_BLOCK).require(GoalRequirements.biome("Pale Garden", Biomes.PALE_GARDEN)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.BONE_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.BOOKSHELF).require(GoalRequirements.VILLAGE));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.EXPERIENCE_BOTTLE));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.BRICK_WALL));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.TROPICAL_FISH_BUCKET).require(GoalRequirements.WARM_OCEAN));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SULFUR_CUBE_BUCKET).require(GoalRequirements.biome("Sulfur Caves", Biomes.SULFUR_CAVES)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.CLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.COBWEB));
        INSTANCE.register(ObtainItemGoalBuilder.any(Items.COPPER_CHEST.asList().toArray(Item[]::new)).customName(_ -> "Obtain Copper Chest"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DAYLIGHT_DETECTOR));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DEAD_BUSH).require(GoalRequirements.biome("Dead Biome", Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS, Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DETECTOR_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DISPENSER));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.DRIED_KELP_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.EMERALD_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.ENDER_CHEST));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER).customName(_ -> "Obtain every type of Furnace"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.MINECART, Items.CHEST_MINECART, Items.HOPPER_MINECART, Items.TNT_MINECART, Items.FURNACE_MINECART).customName(_ -> "Obtain every type of Minecart"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS).customName(_ -> "Obtain every type of Mushroom"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN).customName(_ -> "Obtain every type of Pumpkin"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.RAW_IRON_BLOCK, Items.RAW_COPPER_BLOCK, Items.RAW_GOLD_BLOCK).customName(_ -> "Obtain every type of Raw Ore Block"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.TORCH, Items.SOUL_TORCH, Items.COPPER_TORCH, Items.REDSTONE_TORCH).customName(_ -> "Obtain every type of Torch"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.FLOWERING_AZALEA).require(GoalRequirements.biome("Lush Caves", Biomes.LUSH_CAVES)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.GILDED_BLACKSTONE));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.HEART_OF_THE_SEA).require(GoalRequirements.structure("Shipwreck", BuiltinStructures.SHIPWRECK, BuiltinStructures.SHIPWRECK_BEACHED)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.LODESTONE));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.MOSSY_STONE_BRICK_WALL));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.MUD_BRICK_WALL).require(GoalRequirements.biome("Mangrove Swamp", Biomes.MANGROVE_SWAMP)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.NETHERITE_SCRAP));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.PISTON));
        INSTANCE.register(ObtainItemGoalBuilder.any(ItemUtil.POTTERY_SHERDS.toArray(Item[]::new)).require(GoalRequirements.structure("Suspicious Structure", BuiltinStructures.TRAIL_RUINS, BuiltinStructures.TRIAL_CHAMBERS, BuiltinStructures.OCEAN_RUIN_WARM, BuiltinStructures.OCEAN_RUIN_COLD, BuiltinStructures.DESERT_PYRAMID)).customName(_ -> "Obtain any Pottery Sherd"));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.POWDER_SNOW_BUCKET).require(GoalRequirements.SNOWY));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.POWERED_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.RED_NETHER_BRICKS));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.COMPARATOR));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.REDSTONE_LAMP));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.REPEATER));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.RESIN_BRICK_WALL).require(GoalRequirements.biome("Pale Garden", Biomes.PALE_GARDEN)));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SCAFFOLDING).require(GoalRequirements.JUNGLE));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SMOOTH_BASALT));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SMOOTH_QUARTZ_STAIRS));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SOUL_LANTERN));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.SPONGE).require(GoalRequirements.structure("Ocean Monument", BuiltinStructures.OCEAN_MONUMENT)));
        INSTANCE.register(ObtainItemGoalBuilder.any(Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL).customName(_ -> "Obtain Suspicious Block")
                .require(GoalRequirements.SUSPICIOUS));
        INSTANCE.register(ObtainItemGoalBuilder.all(Items.TNT));
        INSTANCE.register(ObtainItemGoalBuilder.any(ItemUtil.HANGING_SIGN.toArray(Item[]::new)).customName(_ -> "Obtain Hanging Sign"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(2, 4, ItemUtil.ARMOR_TRIM.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Armor Trims"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(2, 5, ItemUtil.HORSE_ARMOR.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Horse Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(2, 5, ItemUtil.BANNER_PATTERN.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Banner Patterns"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(2, 5, ItemUtil.SAPLING.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Saplings"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(2, 5, ItemUtil.SEED.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Seeds"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(4, 7, ItemUtil.BUCKET.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Buckets"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(3, 7, ItemUtil.FLOWER.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Flowers"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(4, 7, ItemUtil.WORKSTATION.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Workstations"));

        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.FLOWER_POT), Stats.POT_FLOWER).customName(_ -> "Pot any Flower"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.CAKE), Stats.EAT_CAKE_SLICE).customName(_ -> "Eat a slice of Cake"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.NOTE_BLOCK), Stats.TUNE_NOTEBLOCK).customName(_ -> "Tune a Note Block"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.JUKEBOX), Stats.PLAY_RECORD).customName(_ -> "Play a Music Disk in a Jukebox"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.CAULDRON), Stats.FILL_CAULDRON).customName(_ -> "Fill a Cauldron with Water"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.LOOM), Stats.INTERACT_WITH_LOOM).customName(_ -> "Use a Loom"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.GRINDSTONE), Stats.INTERACT_WITH_GRINDSTONE).customName(_ -> "Use a Grindstone"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.STONECUTTER), Stats.INTERACT_WITH_STONECUTTER).customName(_ -> "Use a Stonecutter"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.SMITHING_TABLE), Stats.INTERACT_WITH_SMITHING_TABLE).customName(_ -> "Use a Smithing Table"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.ANVIL), Stats.INTERACT_WITH_ANVIL).customName(_ -> "Use an Anvil"));
        INSTANCE.register(ChangeStatisticGoalBuilder.count(500, 3000, 100, i -> i*100, i -> i / 100, "Distance to Boat", "Distance Boated", () -> ItemTextureExtractor.item(Items.OAK_BOAT), Stats.BOAT_ONE_CM).customName(d -> "Boat " + d + "m"));
        INSTANCE.register(ChangeStatisticGoalBuilder.count(500, 2000, 100, i -> i*100, i -> i / 100, "Distance to Sprint", "Distance Sprinted", () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/mob_effect/speed.png")), Stats.SPRINT_ONE_CM).customName(d -> "Sprint " + d + "m"));
        INSTANCE.register(ChangeStatisticGoalBuilder.any(() -> ItemTextureExtractor.item(Items.CAULDRON), Stats.CLEAN_ARMOR, Stats.CLEAN_BANNER, Stats.CLEAN_SHULKER_BOX).customName(_ -> "Clean an Item in a Cauldron"));

        INSTANCE.register(MineBlockGoalBuilder.any(Blocks.SPAWNER, Blocks.TRIAL_SPAWNER).customName(_ -> "Mine a Spawner")
                .require(GoalRequirements.structure("Spawner Structure", BuiltinStructures.MINESHAFT, BuiltinStructures.MINESHAFT_MESA, BuiltinStructures.STRONGHOLD, BuiltinStructures.TRIAL_CHAMBERS)));
        INSTANCE.register(MineBlockGoalBuilder.any(Blocks.CRAFTER));
        INSTANCE.register(MineBlockGoalBuilder.any(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE).customName(_ -> "Mine Diamond Ore"));
        INSTANCE.register(MineBlockGoalBuilder.any(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE).customName(_ -> "Mine Emerald Ore"));
        INSTANCE.register(MineBlockGoalBuilder.any(Blocks.TURTLE_EGG).require(GoalRequirements.biome(Biomes.BEACH)));
        INSTANCE.register(MineBlockGoalBuilder.unique(3, Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE).customName(_ -> "Mine 3 types of Ice")
                .require(GoalRequirements.SNOWY).customTextureExtractor(_ -> new CornerIconTextureExtractor(ItemTextureExtractor.cycleItems(List.of(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE)), ItemTextureExtractor.item(Items.IRON_PICKAXE), 10)));

        INSTANCE.register(ReachExperienceLevelGoalBuilder.of(10, 30, 1));

        INSTANCE.register(TameAnimalGoalBuilder.any(EntityTypes.CAT).require(GoalRequirements.VILLAGE.or(GoalRequirements.structure("Swamp Hut", BuiltinStructures.SWAMP_HUT))));
        INSTANCE.register(TameAnimalGoalBuilder.any(EntityTypes.HORSE).require(GoalRequirements.biome("Horse Biome", Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA)));
        INSTANCE.register(TameAnimalGoalBuilder.any(EntityTypes.NAUTILUS).require(GoalRequirements.OCEAN));
        INSTANCE.register(TameAnimalGoalBuilder.any(EntityTypes.PARROT).require(GoalRequirements.JUNGLE));
        INSTANCE.register(TameAnimalGoalBuilder.any(EntityTypes.WOLF).require(GoalRequirements.biome("Wolf Biome", Biomes.FOREST, Biomes.GROVE, Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.SPARSE_JUNGLE, Biomes.SAVANNA_PLATEAU, Biomes.WOODED_BADLANDS)));

        INSTANCE.register(UseItemOnBlockGoalBuilder.anyBlock(Items.BRUSH, Blocks.SUSPICIOUS_GRAVEL, Blocks.SUSPICIOUS_SAND).customName(_ -> "Brush Suspicious Block").require(GoalRequirements.SUSPICIOUS));
        INSTANCE.register(UseItemOnBlockGoalBuilder.anyBlock(Items.GLOW_INK_SAC, Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN).customName(_ -> "Make a Nether Wood Sign Glow"));

        INSTANCE.register(ObtainItemGoalBuilder.armorPiece(ItemUtil.ARMORS.get(ArmorMaterials.CHAINMAIL).toArray(Item[]::new)).customName(_ -> "Wear a Chain Armor Piece"));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(Items.LEATHER_HELMET));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(Items.LEATHER_CHESTPLATE));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(Items.LEATHER_LEGGINGS));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(Items.LEATHER_BOOTS));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(ArmorMaterials.DIAMOND).customName(_ -> "Wear full Diamond Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(ArmorMaterials.GOLD).customName(_ -> "Wear full Gold Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(ArmorMaterials.IRON).customName(_ -> "Wear full Iron Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(ArmorMaterials.COPPER).customName(_ -> "Wear full Copper Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allEnchantedArmor());
        INSTANCE.register(ObtainItemGoalBuilder.allDifferentArmorMaterial());
        INSTANCE.register(ObtainItemGoalBuilder.allDifferentDyedLeatherArmor());

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

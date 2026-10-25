package me.marin.lockout.lockout;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.miscellanious.PlayerStateGoalBuilder;
import me.marin.lockout.lockout.goal.builder.block.MineBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.block.UseItemOnBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DealDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DeathGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.KillEntityGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.TakeDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.*;
import me.marin.lockout.lockout.goal.builder.experience.ReachExperienceLevelGoalBuilder;
import me.marin.lockout.lockout.goal.builder.inventory.UpdateInventoryGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.*;
import me.marin.lockout.lockout.goal.builder.miscellanious.CustomEvent;
import me.marin.lockout.lockout.goal.builder.miscellanious.CustomEventGoalBuilder;
import me.marin.lockout.lockout.goal.builder.miscellanious.ObtainAdvancementGoalBuilder;
import me.marin.lockout.lockout.goal.builder.miscellanious.RideEntityGoalBuilder;
import me.marin.lockout.lockout.goal.builder.statistic.ChangeStatisticGoalBuilder;
import me.marin.lockout.lockout.goal.builder.statistic.HaveStatusEffectGoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import oshi.util.tuples.Pair;

import java.util.List;

import static me.marin.lockout.lockout.GoalRegistry.INSTANCE;

public class DefaultGoalRegister {
    private static boolean goalsRegistered = false;

    public static synchronized void registerGoals() {
        if (goalsRegistered) {
            return;
        }

        INSTANCE.register(BreakItemGoalBuilder.withComponent(GoalDifficultySupplier.of(0.45f), GoalCategory.ARMOR, DataComponents.EQUIPPABLE)
                .customName(_ -> "Break any armor piece")
                .group(GoalGroups.ARMOR_SPECIAL));
        INSTANCE.register(BreakItemGoalBuilder.withComponent(GoalDifficultySupplier.of(0.4f), GoalCategory.TOOLS, DataComponents.TOOL)
                .customName(_ -> "Break any tool")
                .group(GoalGroups.TOOLS));
        INSTANCE.register(BreakItemGoalBuilder.any(GoalDifficultySupplier.of(0.28f), Items.FISHING_ROD));
        INSTANCE.register(BreakItemGoalBuilder.any(GoalDifficultySupplier.of(0.25f), Items.SHIELD));

        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.65f), EntityTypes.ALLAY).require(GoalRequirements.ALLAY_STRUCTURE));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.28f), EntityTypes.ARMADILLO).require(GoalRequirements.SAVANNA));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.32f), EntityTypes.CAMEL).require(GoalRequirements.DESERT.or(GoalRequirements.DESERT_VILLAGE)));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.18f), EntityTypes.CHICKEN));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.18f), EntityTypes.COW, EntityTypes.MOOSHROOM).customName(_ -> "Breed Cow"));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.42f), EntityTypes.FOX).require(GoalRequirements.TAIGA));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.32f), EntityTypes.FROG).require(GoalRequirements.SWAMP));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.32f), EntityTypes.GOAT).require(GoalRequirements.SNOWY_MOUNTAINS));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.68f), EntityTypes.HOGLIN));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.18f), EntityTypes.PIG));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.32f), EntityTypes.RABBIT));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.18f), EntityTypes.SHEEP));
        INSTANCE.register(BreedAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.68f), EntityTypes.STRIDER));
        INSTANCE.register(BreedAnimalGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(4, 8).inRange(0.3f, 0.55f), 4,8));

        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.68f), Potions.HEALING, Potions.STRONG_HEALING));
        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.72f), Potions.INVISIBILITY, Potions.LONG_INVISIBILITY));
        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.68f), Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON));
        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.68f), Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS));
        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.72f), Potions.WATER_BREATHING, Potions.LONG_WATER_BREATHING));
        INSTANCE.register(BrewPotionGoalBuilder.effect(GoalDifficultySupplier.of(0.68f), Potions.WEAKNESS, Potions.LONG_WEAKNESS));
        INSTANCE.register(BrewPotionGoalBuilder.type(GoalDifficultySupplier.of(0.99f), Items.LINGERING_POTION).customName(_ -> "Brew any Lingering Potion"));

        INSTANCE.register(CompostItemsGoalBuilder.uniqueWithComponent(GoalDifficultySupplier.<Integer>linear(3, 7).inRange(0.25f, 0.55f), 3, 7, DataComponents.FOOD)
                .customName(n -> "Compost " + n + " Unique Foods")
        );

        INSTANCE.register(SpawnEntityGoalBuilder.any(GoalDifficultySupplier.of(0.82f), EntityTypes.IRON_GOLEM).group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(SpawnEntityGoalBuilder.any(GoalDifficultySupplier.of(0.42f), EntityTypes.COPPER_GOLEM));
        INSTANCE.register(SpawnEntityGoalBuilder.any(GoalDifficultySupplier.of(0.3f), EntityTypes.SNOW_GOLEM).require(GoalRequirements.SNOWY));

        INSTANCE.register(CraftItemGoalBuilder.any(GoalDifficultySupplier.of(0.72f), ItemUtil.ARMOR_TRIM.toArray(Item[]::new)).customName(_ -> "Duplicate any Armor Trim"));
        INSTANCE.register(CraftItemGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(20, 100).inRange(0.5f, 0.9f), 20, 100, 10));

        INSTANCE.register(DealDamageGoalBuilder.total(GoalDifficultySupplier.<Integer>linear(100, 500).inRange(0.45f, 0.8f), 100, 500, 25));
        INSTANCE.register(TakeDamageGoalBuilder.total(GoalDifficultySupplier.<Integer>linear(50, 300).inRange(0.3f, 0.65f), 50, 300, 10));

        INSTANCE.register(ObtainItemGoalBuilder.shieldWithBanner(GoalDifficultySupplier.of(0.4f)));

        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.05f), () -> new StackingTextureExtractor(List.of(SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_empty")), SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/air_bursting"))), 0), DamageTypes.DROWN)
                .customName(_ -> "Die by Drowning"));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.99f), () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/spark_4.png")), DamageTypes.FELL_OUT_OF_WORLD)
                .customName(_ -> "Die by Falling in Void"));
        INSTANCE.register(DeathGoalBuilder.fallLocation(GoalDifficultySupplier.of(0.08f), new Pair<>(FallLocation.VINES, Items.VINE), new Pair<>(FallLocation.TWISTING_VINES, Items.TWISTING_VINES), new Pair<>(FallLocation.WEEPING_VINES, Items.WEEPING_VINES))
                .customName(_ -> "Die by Falling of Vines"));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.32f), () -> SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/heart/frozen_full")), DamageTypes.FREEZE)
                .customName(_ -> "Die by Freezing")
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.3f), () -> new ItemTextureExtractor(ItemUtil.applyComponent(Items.POTION.getDefaultInstance(), DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING))), DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC)
                .customName(_ -> "Die by Magic"));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.74f), () -> ItemTextureExtractor.cycleItems(List.of(Items.BED.red(), Items.RESPAWN_ANCHOR)), DamageTypes.BAD_RESPAWN_POINT)
                .customName(_ -> "Die to [Intentional Game Design]"));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.15f), EntityTypes.BEE));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.08f), () -> ItemTextureExtractor.item(Items.SWEET_BERRIES), DamageTypes.SWEET_BERRY_BUSH)
                .customName(_ -> "Die to Berry Bush")
                .require(GoalRequirements.TAIGA));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.08f), () -> ItemTextureExtractor.item(Items.CACTUS), DamageTypes.CACTUS)
                .customName(_ -> "Die to Cactus")
                .require(GoalRequirements.DESERT_LIKE));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.74f), () -> ItemTextureExtractor.item(Items.ANVIL), DamageTypes.FALLING_ANVIL)
                .customName(_ -> "Die to falling Anvil")
                .group(GoalGroups.IRON_HEAVY));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.28f), () -> ItemTextureExtractor.cycleItems(List.of(Items.POINTED_DRIPSTONE, Items.SULFUR_SPIKE)), DamageTypes.FALLING_STALACTITE)
                .customName(_ -> "Die to falling Stalactite")
                .require(GoalRequirements.SPIKY_CAVES));
        INSTANCE.register(DeathGoalBuilder.type(GoalDifficultySupplier.of(0.3f), () -> ItemTextureExtractor.item(Items.FIREWORK_ROCKET), DamageTypes.FIREWORKS)
                .customName(_ -> "Die to Firework Rocket"));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.55f), EntityTypes.IRON_GOLEM).require(GoalRequirements.VILLAGE));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.55f), EntityTypes.POLAR_BEAR).require(GoalRequirements.POLAR));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.2f), EntityTypes.PUFFERFISH).require(GoalRequirements.WARM_OCEAN));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.3f), EntityTypes.TNT_MINECART).customName(_ -> "Die to TNT Minecart"));
        INSTANCE.register(DeathGoalBuilder.entity(GoalDifficultySupplier.of(0.55f), EntityTypes.WARDEN).require(GoalRequirements.DEEP_DARK));

        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.18f), Items.HONEY_BOTTLE).customName(_ -> "Drink Honey Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.anyWithComponent(GoalDifficultySupplier.of(0.15f), DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER), "WATER_BOTTLE", "Water Bottle", Items.POTION)
                .customName(_ -> "Drink Water Bottle"));
        INSTANCE.register(ConsumeItemGoalBuilder.uniqueWithComponent(GoalDifficultySupplier.<Integer>linear(5, 25).inRange(0.25f, 0.55f), 5, 25, DataComponents.FOOD)
                .customName(n -> "Eat " + n + " Unique Foods"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.18f), Items.GLOW_BERRIES).require(GoalRequirements.LUSH_CAVES)
                .customName(_ -> "Eat a Glow Berry"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.18f), Items.POISONOUS_POTATO).customName(_ -> "Eat a Poisonous Potato"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.18f), Items.COOKIE).require(GoalRequirements.JUNGLE).customName(_ -> "Eat a Cookie"));
        INSTANCE.register(ConsumeItemGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(2, 4).inRange(0.3f, 0.55f), 2, 4, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.MUSHROOM_STEW, Items.SUSPICIOUS_STEW)
                .customName(n -> "Eat " + n + " Unique Soups"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.35f), Items.BEETROOT_SOUP).customName(_ -> "Eat Beetroot Soup"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.18f), Items.PUMPKIN_PIE).customName(_ -> "Eat Pumpkin Pie"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.15f), Items.RABBIT_STEW).customName(_ -> "Eat Rabbit Stew"));
        INSTANCE.register(ConsumeItemGoalBuilder.any(GoalDifficultySupplier.of(0.15f), Items.SUSPICIOUS_STEW).customName(_ -> "Eat Suspicious Stew"));
        INSTANCE.register(ConsumeItemGoalBuilder.whileHavingEffect(GoalDifficultySupplier.of(0.18f), Items.MILK_BUCKET).customName(_ -> "Remove Effect using Milk Bucket"));

        INSTANCE.register(PlayerStateGoalBuilder.emptyHungerBar(GoalDifficultySupplier.of(0.08f)));
        INSTANCE.register(PlayerStateGoalBuilder.heightAbove(GoalDifficultySupplier.of(0.42f), 320, ServerLevel.OVERWORLD).customName(_ -> "Reach height limit"));
        INSTANCE.register(PlayerStateGoalBuilder.heightAbove(GoalDifficultySupplier.of(0.7f), 128, ServerLevel.NETHER).customName(_ -> "Reach Nether Roof")
                .customTextureExtractor(_ -> new CornerIconTextureExtractor(
                        ItemTextureExtractor.item(Items.BEDROCK),
                        GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/up_arrow.png")),
                8))
                .defaultEnabled(false));

        INSTANCE.register(AngerMobGoalBuilder.any(GoalDifficultySupplier.of(0.22f), EntityTypes.ZOMBIFIED_PIGLIN));

        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.62f), "story/enter_the_nether").customName(_ -> "Enter The Nether"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.98f), "story/enter_the_end").customName(_ -> "Enter The End"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.84f), "story/follow_ender_eye").customName(_ -> "Enter a Stronghold"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.72f), "nether/charge_respawn_anchor").customName(_ -> "Fully charge a Respawn Anchor"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.5f), "adventure/whos_the_pillager_now").customName(_ -> "Kill a Pillager using a Crossbow")
                .require(GoalRequirements.PILLAGER_OUTPOST));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.4f), "adventure/bullseye"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.4f), "adventure/spear_many_mobs"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.52f), "nether/distract_piglin"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.5f), "adventure/sniper_duel"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.66f), "husbandry/place_dried_ghast_in_water"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.66f), "nether/ride_strider"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.2f), "husbandry/wax_on"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.2f), "husbandry/wax_off"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(5, 30).inRange(0.4f, 0.8f), 5, 30, 1));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.78f), "adventure/spyglass_at_parrot", "adventure/spyglass_at_ghast", "adventure/spyglass_at_dragon").customName(_ -> "Obtain any Spyglass Advancement"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.32f), "adventure/trade").customName(_ -> "Trade with Villager"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.68f), "nether/brew_potion").customName(_ -> "Brew a Potion"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.32f), "story/enchant_item").customName(_ -> "Enchant Item at Enchanting Table"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.2f), "husbandry/make_a_sign_glow").customName(_ -> "Use Glow Ink on a Sign"));
        INSTANCE.register(ObtainAdvancementGoalBuilder.any(GoalDifficultySupplier.of(0.66f), "nether/explore_nether"));

        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.82f), EntityTypes.END_CRYSTAL).customName(_ -> "Explode End Crystal").customTextureExtractor(_ -> ItemTextureExtractor.item(Items.END_CRYSTAL)));

        INSTANCE.register(KillEntityGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(5, 15).inRange(0.45f, 0.75f), 5,15, EntityUtil.HOSTILE.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Unique Hostile Mobs"));
        INSTANCE.register(KillEntityGoalBuilder.total(GoalDifficultySupplier.<Integer>linear(10, 30).inRange(0.3f, 0.6f), 10, 30, 1, EntityUtil.ARTHROPODS.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Arthropods"));
        INSTANCE.register(KillEntityGoalBuilder.total(GoalDifficultySupplier.<Integer>linear(10, 30).inRange(0.3f, 0.6f), 10, 30, 1, EntityUtil.UNDEAD.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Undead Mobs"));
        INSTANCE.register(KillEntityGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(3, 6).inRange(0.6f, 0.85f), 3,6, EntityUtil.RAID.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Raid Mobs"));
        INSTANCE.register(KillEntityGoalBuilder.total(GoalDifficultySupplier.<Integer>linear(50, 150).inRange(0.5f, 0.82f), 50,150, 5, EntityUtil.HOSTILE.toArray(EntityType[]::new)).customName(n -> "Kill " + n + " Hostile Mobs"));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.22f), EntityTypes.BAT));
        INSTANCE.register(KillEntityGoalBuilder.coloredSheep(GoalDifficultySupplier.of(0.22f)));
        INSTANCE.register(KillEntityGoalBuilder.deathType(GoalDifficultySupplier.of(0.78f), DamageTypes.WIND_CHARGE, () -> ItemTextureExtractor.item(Items.WIND_CHARGE), EntityTypes.BREEZE)
                .require(GoalRequirements.TRIAL_CHAMBERS));
        INSTANCE.register(KillEntityGoalBuilder.deathType(GoalDifficultySupplier.of(0.78f), DamageTypes.THROWN, () -> ItemTextureExtractor.item(Items.SNOWBALL), EntityTypes.BLAZE)
                .require(GoalRequirements.SNOWY).customName(_ -> "Kill Blaze using Snowball").defaultEnabled(false));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.4f), EntityTypes.BOGGED)
                .require(GoalRequirements.SWAMP.or(GoalRequirements.TRIAL_CHAMBERS)));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.9f), EntityTypes.ELDER_GUARDIAN)
                .require(GoalRequirements.OCEAN_MONUMENT));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.62f), EntityTypes.GUARDIAN)
                .require(GoalRequirements.OCEAN_MONUMENT));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.3f), EntityTypes.HUSK)
                .require(GoalRequirements.DESERT));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.38f), EntityTypes.SILVERFISH));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.22f), EntityTypes.SNOW_GOLEM)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoalBuilder.dimension(GoalDifficultySupplier.of(0.7f), ServerLevel.NETHER, () -> ItemTextureExtractor.item(Items.NETHERRACK), EntityTypes.SNOW_GOLEM)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.4f), EntityTypes.STRAY)
                .require(GoalRequirements.SNOWY));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.98f), EntityTypes.WARDEN)
                .require(GoalRequirements.ANCIENT_CITY));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.4f), EntityTypes.WITCH));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.66f), EntityTypes.ZOGLIN));
        INSTANCE.register(KillEntityGoalBuilder.any(GoalDifficultySupplier.of(0.4f), EntityTypes.ZOMBIE_VILLAGER));

        INSTANCE.register(ObtainItemGoalBuilder.colored(GoalDifficultySupplier.of(0.55f), Items.CONCRETE, 64, "CONCRETE"));
        INSTANCE.register(ObtainItemGoalBuilder.colored(GoalDifficultySupplier.of(0.28f), Items.WOOL, 64, "WOOL"));
        INSTANCE.register(ObtainItemGoalBuilder.colored(GoalDifficultySupplier.of(0.42f), Items.HARNESS, 1, "HARNESS"));
        INSTANCE.register(ObtainItemGoalBuilder.withCount(GoalDifficultySupplier.of(0.08f), Items.COARSE_DIRT, 64));
        INSTANCE.register(ObtainItemGoalBuilder.anyFullStack(GoalDifficultySupplier.of(0.05f)));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.48f), Items.ACTIVATOR_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.withCount(GoalDifficultySupplier.of(0.3f), Items.GLOW_LICHEN, 16));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE, Items.GOLDEN_SPEAR).customName(_ -> "Obtain all Gold Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.3f), Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SPEAR).customName(_ -> "Obtain all Iron Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.12f), Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SPEAR).customName(_ -> "Obtain all Stone Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.07f), Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SPEAR).customName(_ -> "Obtain all Wooden Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.4f), Items.COPPER_SWORD, Items.COPPER_AXE, Items.COPPER_PICKAXE, Items.COPPER_SHOVEL, Items.COPPER_HOE, Items.COPPER_SPEAR).customName(_ -> "Obtain all Copper Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.9f), Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SPEAR).customName(_ -> "Obtain all Diamond Tools"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.BELL));
        INSTANCE.register(ObtainItemGoalBuilder.colored(GoalDifficultySupplier.of(0.32f), Items.GLAZED_TERRACOTTA, 1, "GLAZED_TERRACOTTA"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.52f), Items.RESIN_BLOCK).require(GoalRequirements.PALE_GARDEN));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.BONE_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.BOOKSHELF).require(GoalRequirements.VILLAGE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.3f), Items.EXPERIENCE_BOTTLE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.BRICK_WALL));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.52f), Items.TROPICAL_FISH_BUCKET).require(GoalRequirements.WARM_OCEAN));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.78f), Items.SULFUR_CUBE_BUCKET).require(GoalRequirements.SULFUR_CAVES));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.CLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.COBWEB));
        INSTANCE.register(ObtainItemGoalBuilder.any(GoalDifficultySupplier.of(0.35f), Items.COPPER_CHEST.asList().toArray(Item[]::new)).customName(_ -> "Obtain Copper Chest"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.DAYLIGHT_DETECTOR));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.38f), Items.DEAD_BUSH).require(GoalRequirements.DEAD_BIOME));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.48f), Items.DETECTOR_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.DISPENSER));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.DRIED_KELP_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.78f), Items.EMERALD_BLOCK));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.68f), Items.ENDER_CHEST));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER).customName(_ -> "Obtain every type of Furnace"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.78f), Items.MINECART, Items.CHEST_MINECART, Items.HOPPER_MINECART, Items.TNT_MINECART, Items.FURNACE_MINECART).customName(_ -> "Obtain every type of Minecart"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.66f), Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS).customName(_ -> "Obtain every type of Mushroom"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN).customName(_ -> "Obtain every type of Pumpkin"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.68f), Items.RAW_IRON_BLOCK, Items.RAW_COPPER_BLOCK, Items.RAW_GOLD_BLOCK).customName(_ -> "Obtain every type of Raw Ore Block"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.66f), Items.TORCH, Items.SOUL_TORCH, Items.COPPER_TORCH, Items.REDSTONE_TORCH).customName(_ -> "Obtain every type of Torch"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.FLOWERING_AZALEA).require(GoalRequirements.LUSH_CAVES));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.68f), Items.GILDED_BLACKSTONE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.88f), Items.HEART_OF_THE_SEA).require(GoalRequirements.BURIED_TREASURE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.3f), Items.LODESTONE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.MOSSY_STONE_BRICK_WALL));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.MUD_BRICK_WALL).require(GoalRequirements.MANGROVE_SWAMP));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.85f), Items.NETHERITE_SCRAP));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.PISTON));
        INSTANCE.register(ObtainItemGoalBuilder.any(GoalDifficultySupplier.of(0.52f), ItemUtil.POTTERY_SHERDS.toArray(Item[]::new)).require(GoalRequirements.SUSPICIOUS).customName(_ -> "Obtain any Pottery Sherd"));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.3f), Items.POWDER_SNOW_BUCKET).require(GoalRequirements.SNOWY));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.48f), Items.POWERED_RAIL));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.66f), Items.RED_NETHER_BRICKS));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.COMPARATOR));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.REDSTONE_LAMP));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.REPEATER));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.52f), Items.RESIN_BRICK_WALL).require(GoalRequirements.PALE_GARDEN));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.SCAFFOLDING).require(GoalRequirements.JUNGLE));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.3f), Items.SMOOTH_BASALT));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.66f), Items.SMOOTH_QUARTZ_STAIRS));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.66f), Items.SOUL_LANTERN));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.78f), Items.SULFUR_CUBE_BUCKET).require(GoalRequirements.SULFUR_CAVES));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.SHELF_MUSHROOM).require(GoalRequirements.DAPPLED_FOREST));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.88f), Items.SPONGE).require(GoalRequirements.OCEAN_MONUMENT));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.35f), Items.WRITTEN_BOOK).customName(_ -> "Write a Book"));
        INSTANCE.register(ObtainItemGoalBuilder.any(GoalDifficultySupplier.of(0.52f), Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL).customName(_ -> "Obtain Suspicious Block")
                .require(GoalRequirements.SUSPICIOUS));
        INSTANCE.register(ObtainItemGoalBuilder.all(GoalDifficultySupplier.of(0.22f), Items.TNT));
        INSTANCE.register(ObtainItemGoalBuilder.any(GoalDifficultySupplier.of(0.35f), ItemUtil.HANGING_SIGN.toArray(Item[]::new)).customName(_ -> "Obtain Hanging Sign"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(2, 4).inRange(0.55f, 0.85f), 2, 4, ItemUtil.ARMOR_TRIM.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Armor Trims"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(2, 5).inRange(0.4f, 0.72f), 2, 5, ItemUtil.HORSE_ARMOR.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Horse Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(2, 5).inRange(0.4f, 0.72f), 2, 5, ItemUtil.BANNER_PATTERN.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Banner Patterns"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(2, 5).inRange(0.25f, 0.55f), 2, 5, ItemUtil.SAPLING.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Saplings"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(2, 5).inRange(0.25f, 0.55f), 2, 5, ItemUtil.SEED.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Seeds"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(4, 7).inRange(0.4f, 0.7f), 4, 7, ItemUtil.BUCKET.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Buckets"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(3, 7).inRange(0.25f, 0.55f), 3, 7, ItemUtil.FLOWER.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Flowers"));
        INSTANCE.register(ObtainItemGoalBuilder.atLeast(GoalDifficultySupplier.<Integer>linear(4, 7).inRange(0.4f, 0.7f), 4, 7, ItemUtil.WORKSTATION.toArray(Item[]::new)).customName(n -> "Obtain " + n + " Unique Workstations"));
        INSTANCE.register(ObtainItemGoalBuilder.allDistinct(GoalDifficultySupplier.of(0.85f)));

        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.1f), () -> ItemTextureExtractor.item(Items.FLOWER_POT), Stats.POT_FLOWER).customName(_ -> "Pot any Flower"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.38f), () -> ItemTextureExtractor.item(Items.CAKE), Stats.EAT_CAKE_SLICE).customName(_ -> "Eat a slice of Cake"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.2f), () -> ItemTextureExtractor.item(Items.NOTE_BLOCK), Stats.TUNE_NOTEBLOCK).customName(_ -> "Tune a Note Block"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.2f), () -> ItemTextureExtractor.item(Items.JUKEBOX), Stats.PLAY_RECORD).customName(_ -> "Play a Music Disk in a Jukebox"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.12f), () -> ItemTextureExtractor.item(Items.CAULDRON), Stats.FILL_CAULDRON).customName(_ -> "Fill a Cauldron with Water"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.1f), () -> ItemTextureExtractor.item(Items.LOOM), Stats.INTERACT_WITH_LOOM).customName(_ -> "Use a Loom"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.32f), () -> ItemTextureExtractor.item(Items.GRINDSTONE), Stats.INTERACT_WITH_GRINDSTONE).customName(_ -> "Use a Grindstone"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.1f), () -> ItemTextureExtractor.item(Items.STONECUTTER), Stats.INTERACT_WITH_STONECUTTER).customName(_ -> "Use a Stonecutter"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.32f), () -> ItemTextureExtractor.item(Items.SMITHING_TABLE), Stats.INTERACT_WITH_SMITHING_TABLE).customName(_ -> "Use a Smithing Table"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.7f), () -> ItemTextureExtractor.item(Items.ANVIL), Stats.INTERACT_WITH_ANVIL).customName(_ -> "Use an Anvil"));
        INSTANCE.register(ChangeStatisticGoalBuilder.countCustom(GoalDifficultySupplier.<Integer>linear(50000, 300000).inRange(0.3f, 0.75f), 500, 3000, 100, i -> i*100, i -> i / 100, "Distance to Boat", "Distance Boated", () -> ItemTextureExtractor.item(Items.OAK_BOAT), Stats.BOAT_ONE_CM).customName(d -> "Boat " + d + "m"));
        INSTANCE.register(ChangeStatisticGoalBuilder.countCustom(GoalDifficultySupplier.<Integer>linear(50000, 200000).inRange(0.2f, 0.58f), 500, 2000, 100, i -> i*100, i -> i / 100, "Distance to Sprint", "Distance Sprinted", () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/mob_effect/speed.png")), Stats.SPRINT_ONE_CM).customName(d -> "Sprint " + d + "m"));
        INSTANCE.register(ChangeStatisticGoalBuilder.custom(GoalDifficultySupplier.of(0.15f), () -> ItemTextureExtractor.item(Items.CAULDRON), Stats.CLEAN_ARMOR, Stats.CLEAN_BANNER, Stats.CLEAN_SHULKER_BOX).customName(_ -> "Clean an Item in a Cauldron"));
        INSTANCE.register(ChangeStatisticGoalBuilder.item(GoalDifficultySupplier.of(0.25f), Stats.ITEM_USED, Items.GOAT_HORN).customName(_ -> "Toot a Goat Horn"));
        INSTANCE.register(ChangeStatisticGoalBuilder.itemWhileRiding(GoalDifficultySupplier.of(0.4f), Stats.ITEM_USED, Items.CARROT_ON_A_STICK, EntityTypes.PIG).customName(_ -> "Use Carrot on a Stick to boost a Pig"));

        INSTANCE.register(MineBlockGoalBuilder.any(GoalDifficultySupplier.of(0.7f), Blocks.SPAWNER, Blocks.TRIAL_SPAWNER).customName(_ -> "Mine a Spawner")
                .require(GoalRequirements.SPAWNER_STRUCTURE));
        INSTANCE.register(MineBlockGoalBuilder.any(GoalDifficultySupplier.of(0.3f), Blocks.CRAFTER));
        INSTANCE.register(MineBlockGoalBuilder.any(GoalDifficultySupplier.of(0.65f), Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE).customName(_ -> "Mine Diamond Ore"));
        INSTANCE.register(MineBlockGoalBuilder.any(GoalDifficultySupplier.of(0.72f), Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE).customName(_ -> "Mine Emerald Ore"));
        INSTANCE.register(MineBlockGoalBuilder.any(GoalDifficultySupplier.of(0.38f), Blocks.TURTLE_EGG).require(GoalRequirements.BEACH));
        INSTANCE.register(MineBlockGoalBuilder.unique(3, GoalDifficultySupplier.of(0.48f), Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE).customName(_ -> "Mine 3 types of Ice")
                .require(GoalRequirements.SNOWY).customTextureExtractor(_ -> new CornerIconTextureExtractor(ItemTextureExtractor.cycleItems(List.of(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE)), ItemTextureExtractor.item(Items.IRON_PICKAXE), 10)));

        INSTANCE.register(ReachExperienceLevelGoalBuilder.of(GoalDifficultySupplier.<Integer>linear(10, 30).inRange(0.3f, 0.65f), 10, 30, 1));

        INSTANCE.register(TameAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.3f), EntityTypes.CAT).require(GoalRequirements.VILLAGE.or(GoalRequirements.SWAMP_HUT)));
        INSTANCE.register(TameAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.3f), EntityTypes.HORSE).require(GoalRequirements.HORSE_BIOME));
        INSTANCE.register(TameAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.58f), EntityTypes.NAUTILUS).require(GoalRequirements.OCEAN));
        INSTANCE.register(TameAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.42f), EntityTypes.PARROT).require(GoalRequirements.JUNGLE));
        INSTANCE.register(TameAnimalGoalBuilder.any(GoalDifficultySupplier.of(0.3f), EntityTypes.WOLF).require(GoalRequirements.WOLF_BIOME));

        INSTANCE.register(UseItemOnBlockGoalBuilder.anyBlock(GoalDifficultySupplier.of(0.48f), Items.BRUSH, Blocks.SUSPICIOUS_GRAVEL, Blocks.SUSPICIOUS_SAND).customName(_ -> "Brush Suspicious Block").require(GoalRequirements.SUSPICIOUS));
        INSTANCE.register(UseItemOnBlockGoalBuilder.anyBlock(GoalDifficultySupplier.of(0.66f), Items.GLOW_INK_SAC, Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN).customName(_ -> "Make a Nether Wood Sign Glow"));
        INSTANCE.register(UseItemOnBlockGoalBuilder.anyItem(GoalDifficultySupplier.of(0.2f), Blocks.CANDLE, Items.FLINT_AND_STEEL, Items.FIRE_CHARGE).customName(_ -> "Light a Candle"));
        INSTANCE.register(UseItemOnBlockGoalBuilder.anyBlock(GoalDifficultySupplier.of(0.12f), Items.PAINTING, BuiltInRegistries.BLOCK.stream().toArray(Block[]::new)).customName(_ -> "Place a Painting").customId(_ -> "USE_PAINTING_ON_BLOCK").customTextureExtractor(_ -> ItemTextureExtractor.item(Items.PAINTING)));

        INSTANCE.register(ObtainItemGoalBuilder.armorPiece(GoalDifficultySupplier.of(0.6f), ItemUtil.ARMORS.get(ArmorMaterials.CHAINMAIL).toArray(Item[]::new)).customName(_ -> "Wear a Chain Armor Piece"));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(GoalDifficultySupplier.of(0.32f), Items.LEATHER_HELMET));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(GoalDifficultySupplier.of(0.32f), Items.LEATHER_CHESTPLATE));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(GoalDifficultySupplier.of(0.32f), Items.LEATHER_LEGGINGS));
        INSTANCE.register(ObtainItemGoalBuilder.dyedArmorPiece(GoalDifficultySupplier.of(0.32f), Items.LEATHER_BOOTS));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(GoalDifficultySupplier.of(0.95f), ArmorMaterials.DIAMOND).customName(_ -> "Wear full Diamond Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(GoalDifficultySupplier.of(0.58f), ArmorMaterials.GOLD).customName(_ -> "Wear full Gold Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(GoalDifficultySupplier.of(0.68f), ArmorMaterials.IRON).customName(_ -> "Wear full Iron Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allArmorOfMaterial(GoalDifficultySupplier.of(0.55f), ArmorMaterials.COPPER).customName(_ -> "Wear full Copper Armor"));
        INSTANCE.register(ObtainItemGoalBuilder.allEnchantedArmor(GoalDifficultySupplier.of(0.78f)));
        INSTANCE.register(ObtainItemGoalBuilder.allDifferentArmorMaterial(GoalDifficultySupplier.of(0.4f)));
        INSTANCE.register(ObtainItemGoalBuilder.allDifferentDyedLeatherArmor(GoalDifficultySupplier.of(0.65f)));

        INSTANCE.register(LeashEntityGoalBuilder.any(GoalDifficultySupplier.of(0.45f), EntityTypes.IRON_GOLEM));
        INSTANCE.register(LeashEntityGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(3, 8).inRange(0.35f, 0.62f), 3, 8, 1));

        INSTANCE.register(AgeLockMobGoalBuilder.any(GoalDifficultySupplier.of(0.22f)));

        INSTANCE.register(UpdateInventoryGoalBuilder.fillBlock(GoalDifficultySupplier.of(0.18f), Blocks.DECORATED_POT));
        INSTANCE.register(UpdateInventoryGoalBuilder.fillBlock(GoalDifficultySupplier.of(0.18f), Blocks.CAMPFIRE));
        INSTANCE.register(UpdateInventoryGoalBuilder.fillBlock(GoalDifficultySupplier.of(0.28f), Blocks.CHISELED_BOOKSHELF));
        INSTANCE.register(UpdateInventoryGoalBuilder.fillBlock(GoalDifficultySupplier.of(0.25f), Blocks.COMPOSTER));
        INSTANCE.register(UpdateInventoryGoalBuilder.fillEntity(GoalDifficultySupplier.of(0.18f), EntityTypes.ARMOR_STAND));

        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.25f), MobEffects.ABSORPTION));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.75f), MobEffects.BAD_OMEN).require(GoalRequirements.BAD_OMEN_STRUCTURE));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.15f), MobEffects.JUMP_BOOST));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.75f), MobEffects.MINING_FATIGUE).require(GoalRequirements.OCEAN_MONUMENT));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.38f), MobEffects.NAUSEA).require(GoalRequirements.NAUSEA_BIOME));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.25f), MobEffects.POISON));
        INSTANCE.register(HaveStatusEffectGoalBuilder.any(GoalDifficultySupplier.of(0.15f), MobEffects.WEAKNESS));

        INSTANCE.register(BlockWithItemGoalBuilder.disabled(GoalDifficultySupplier.of(0.25f), Items.SHIELD));

        INSTANCE.register(UseItemOnEntityGoalBuilder.of(GoalDifficultySupplier.of(0.2f), Items.ITEM_FRAME, EntityTypes.ITEM_FRAME).customName(_ -> "Place Item Frame in Item Frame"));
        INSTANCE.register(UseItemOnEntityGoalBuilder.of(GoalDifficultySupplier.of(0.4f), Items.WOLF_ARMOR, EntityTypes.WOLF).customName(_ -> "Put Wolf Armor on Wolf"));
        INSTANCE.register(UseItemOnEntityGoalBuilder.nameTag(GoalDifficultySupplier.of(0.68f), "Dinnerbone", EntityTypes.GHAST));
        INSTANCE.register(UseItemOnEntityGoalBuilder.nameTag(GoalDifficultySupplier.of(0.25f), "jeb_", EntityTypes.SHEEP));
        INSTANCE.register(UseItemOnEntityGoalBuilder.unique(GoalDifficultySupplier.<Integer>linear(5, 20).inRange(0.35f, 0.7f), 5, 20, 1, Items.SPYGLASS).customName(n -> "Spy on " + n + " Unique Mobs"));
        INSTANCE.register(UseItemOnEntityGoalBuilder.of(GoalDifficultySupplier.of(0.2f), Items.SPYGLASS, EntityTypes.IRON_GOLEM).customName(_ -> "Spy on Iron Golem"));
        INSTANCE.register(UseItemOnEntityGoalBuilder.of(GoalDifficultySupplier.of(0.68f), Items.SPYGLASS, EntityTypes.PIGLIN_BRUTE).customName(_ -> "Spy on Piglin Brute"));

        INSTANCE.register(RideEntityGoalBuilder.any(GoalDifficultySupplier.of(0.32f), EntityTypes.MINECART));
        INSTANCE.register(RideEntityGoalBuilder.any(GoalDifficultySupplier.of(0.05f), EntityTypes.CUSHION).customName(_ -> "Sit on a Cushion"));

        INSTANCE.register(CustomEventGoalBuilder.of(GoalDifficultySupplier.of(0.45f), CustomEvent.GEYSER_LAUNCH).require(GoalRequirements.SULFUR_CAVES));

        INSTANCE.register(PlayerStateGoalBuilder.sleepIn(GoalDifficultySupplier.of(0.12f), Blocks.STRAW_BED));
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

package me.marin.lockout.lockout;

import me.marin.lockout.lockout.goal.builder.*;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirements;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import oshi.util.tuples.Pair;

import static me.marin.lockout.lockout.GoalRegistry.INSTANCE;
import static net.minecraft.world.level.biome.Biomes.*;

public class DefaultGoalRegister {
    private static boolean goalsRegistered = false;

    public static synchronized void registerGoals() {
        if (goalsRegistered) {
            return;
        }
        INSTANCE.register(ObtainAllItemGoalBuilder.simple("ALL_WOODEN_TOOLS", GoalCategory.TOOLS, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_SPEAR)
                .customNameExtractor(NameExtractor.simple("Obtain all Wooden Tools")));
        INSTANCE.register(ObtainColoredItemGoalBuilder.withCount("64_WOOL", GoalCategory.OBTAINING_ITEMS, Items.WOOL, 64));
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.WITHER_SKELETON_SKULL)
                .defaultEnabled(false)
        );
        INSTANCE.register(ObtainAllItemGoalBuilder.simple(GoalCategory.OBTAINING_ITEMS, Items.SULFUR_CUBE_BUCKET)
                .require(GoalRequirements.anyBiome("Sulfur Caves", SULFUR_CAVES))
        );
        INSTANCE.register(RideEntityGoalBuilder.simple(GoalCategory.RIDING, EntityTypes.HORSE));
        INSTANCE.register(RideEntityGoalBuilder.simple(GoalCategory.RIDING, EntityTypes.STRIDER)
                .customNameExtractor(NameExtractor.simple("Ride a Strider with custom name"))
        );
        INSTANCE.register(ObtainSomeItemsGoalBuilder.multiple("4_UNIQUE_SEEDS", 4, GoalCategory.OBTAINING_ITEMS, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS)
                .customNameExtractor(NameExtractor.simple("Obtain 4 unique Seeds"))
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
                .customNameExtractor(NameExtractor.simple("Obtain any Spyglass Advancement"))
        );
        INSTANCE.register(AdvancementGoalBuilder.counting("UNIQUE_ADVANCEMENTS", GoalCategory.ADVANCEMENTS, 3, 30));

        goalsRegistered = true;
    }

}

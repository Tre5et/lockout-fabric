package me.marin.lockout.lockout;

import me.marin.lockout.lockout.goal.builder.break_item.BreakItemGoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import net.minecraft.core.component.DataComponents;

import static me.marin.lockout.lockout.GoalRegistry.INSTANCE;

public class DefaultGoalRegister {
    private static boolean goalsRegistered = false;

    public static synchronized void registerGoals() {
        if (goalsRegistered) {
            return;
        }

        INSTANCE.register(BreakItemGoalBuilder.withComponent("ANY_ARMOR_PIECE", GoalCategory.ARMOR, DataComponents.EQUIPPABLE)
                .customName(_ -> "Break any armor piece"));
        INSTANCE.register(BreakItemGoalBuilder.withComponent("ANY_TOOL", GoalCategory.TOOLS, DataComponents.TOOL)
                .customName(_ -> "Break any tool"));

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

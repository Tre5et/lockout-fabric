package me.marin.lockout.lockout.goal.config;

import lombok.Getter;

public enum GoalCategory {
    TOOLS("Tools"),
    MINING("Mining"),
    DIMENSIONS("Dimensions"),
    ARMOR("Armor"),
    TAMING("Taming"),
    BREEDING("Breeding"),
    KILLING("Killing"),
    OBTAINING_ITEMS("Obtaining Items"),
    BREWING("Brewing"),
    BIOMES("Biomes"),
    EATING_DRINKING("Eating/Drinking"),
    MISC_ACTIONS("Misc Actions"),
    SPAWNING("Spawning"),
    ADVANCEMENTS("Advancements"),
    STATUS_EFFECTS("Status Effects"),
    EXPERIENCE("Experience"),
    RIDING("Riding"),
    WORKSTATIONS("Workstations"),
    DEATH_DAMAGE("Death/Damage"),
    TEAM_GOALS("Team Goals");

    @Getter
    private final String name;

    GoalCategory(String name) {
        this.name = name;
    }
}

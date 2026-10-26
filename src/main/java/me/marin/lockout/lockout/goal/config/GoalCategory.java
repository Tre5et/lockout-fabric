package me.marin.lockout.lockout.goal.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public class GoalCategory {
    public static final Codec<GoalCategory> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(GoalCategory::getName)
    ).apply(i, GoalCategory::new));

    public static final GoalCategory TOOLS = new GoalCategory("Tools");
    public static final GoalCategory MINING = new GoalCategory("Mining");
    public static final GoalCategory DIMENSIONS = new GoalCategory("Dimensions");
    public static final GoalCategory ARMOR = new GoalCategory("Armor");
    public static final GoalCategory TAMING = new GoalCategory("Taming");
    public static final GoalCategory BREEDING = new GoalCategory("Breeding");
    public static final GoalCategory KILLING = new GoalCategory("Killing");
    public static final GoalCategory OBTAINING_ITEMS = new GoalCategory("Obtaining Items");
    public static final GoalCategory BREWING = new GoalCategory("Brewing");
    public static final GoalCategory BIOMES = new GoalCategory("Biomes");
    public static final GoalCategory EATING_DRINKING = new GoalCategory("Eating/Drinking");
    public static final GoalCategory MISC_ACTIONS = new GoalCategory("Misc Actions");
    public static final GoalCategory SPAWNING = new GoalCategory("Spawning");
    public static final GoalCategory ADVANCEMENTS = new GoalCategory("Advancements");
    public static final GoalCategory STATUS_EFFECTS = new GoalCategory("Status Effects");
    public static final GoalCategory EXPERIENCE = new GoalCategory("Experience");
    public static final GoalCategory RIDING = new GoalCategory("Riding");
    public static final GoalCategory WORKSTATIONS = new GoalCategory("Workstations");
    public static final GoalCategory DEATH_DAMAGE = new GoalCategory("Death/Damage");
    public static final GoalCategory TEAM_GOALS = new GoalCategory("Team Goals");
    public static final GoalCategory LEASHING = new GoalCategory("Leashing");

    @Getter
    private final String name;

    GoalCategory(String name) {
        this.name = name;
    }
}

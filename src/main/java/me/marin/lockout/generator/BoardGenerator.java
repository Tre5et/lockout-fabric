package me.marin.lockout.generator;

import me.marin.lockout.LocateData;
import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeamServer;
import me.marin.lockout.client.LockoutBoard;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

public class BoardGenerator {

    private final List<GoalBuilder<?>> registeredGoals;
    private final List<LockoutTeamServer> teams;
    private final Map<ResourceKey<Biome>, LocateData> biomes;
    private final Map<ResourceKey<Structure>, LocateData> structures;
    private final int maxRecursionDepth = 100;

    public BoardGenerator(List<GoalBuilder<?>> registeredGoals, List<LockoutTeamServer> teams, Map<ResourceKey<Biome>, LocateData> biomes, Map<ResourceKey<Structure>, LocateData> structures) {
        this.registeredGoals = registeredGoals;
        this.teams = teams;
        this.biomes = biomes;
        this.structures = structures;
    }

    public LockoutBoard generateBoard(int size) {
        return generateBoard(size, 0);
    }

    private LockoutBoard generateBoard(int size, int recursionDepth) {
        // Prevent infinite recursion
        if (recursionDepth >= maxRecursionDepth) {
            Lockout.log("Board generation failed: max recursion depth (" + maxRecursionDepth + ") reached");
            return null;
        }

        Collections.shuffle(registeredGoals);

        List<GoalBuilder<?>> goalBuilders = new ArrayList<>();

        ListIterator<GoalBuilder<?>> it = registeredGoals.listIterator();
        while (goalBuilders.size() < size * size && it.hasNext()) {
            GoalBuilder<?> goal = it.next();

            // Always check enable state first - this applies to ALL goals
            if (!goal.isEnabled()) {
                continue;
            }

            if (!goal.getGroups().stream().allMatch(g -> g.canAdd(goal, goalBuilders))) {
                // Group does not permit this goal to be added.
                continue;
            }

            GoalRequirements goalRequirements = goal.getRequirements();
            if (goalRequirements != null) {
                if (!goalRequirements.isTeamsSizeOk(teams.size())) {
                    continue;
                }
                if (!goalRequirements.isSatisfied(biomes, structures)) {
                    continue;
                }
            }

            goalBuilders.add(goal);
        }

        // If we didn't get enough goals, try again with a new shuffle
        if (goalBuilders.size() < size * size) {
            Lockout.log("Board generation attempt " + (recursionDepth + 1) + ": only got " + goalBuilders.size() + " goals, need " + (size * size));
            return generateBoard(size, recursionDepth + 1);
        }

        // Construct the goals with ramdom options
        List<Goal> goals = new ArrayList<>();
        for(GoalBuilder<?> goal : goalBuilders) {
            goals.add(goal.buildArbitrary());
        }

        // Shuffle the board again. Some goals will always be after some other goals (GoalGroup#requirePredecessor),
        // and shuffle fixes this.
        Collections.shuffle(goals);

        return new LockoutBoard(goals);
    }

}

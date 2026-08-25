package me.marin.lockout.generator;

import me.marin.lockout.Lockout;
import me.marin.lockout.client.LockoutBoard;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;

import java.util.*;

public class BoardGenerator {

    private final List<GoalBuilder<?>> registeredGoals;
    private final GoalRequirementContext context;
    private static final int MAX_RECURSION_DEPTH = 100;

    public BoardGenerator(List<GoalBuilder<?>> registeredGoals, GoalRequirementContext context) {
        this.registeredGoals = registeredGoals;
        this.context = context;
    }

    public LockoutBoard generateBoard(int size) {
        return generateBoard(size, 0);
    }

    private LockoutBoard generateBoard(int size, int recursionDepth) {
        // Prevent infinite recursion
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            Lockout.log("Board generation failed: max recursion depth (" + MAX_RECURSION_DEPTH + ") reached");
            return null;
        }

        Collections.shuffle(registeredGoals);

        List<GoalBuilder<?>> goalBuilders = new ArrayList<>();
        List<Goal<?>> goals = new ArrayList<>();

        ListIterator<GoalBuilder<?>> it = registeredGoals.listIterator();
        while (goals.size() < size * size && it.hasNext()) {
            GoalBuilder<?> goal = it.next();

            // Always check enable state first - this applies to ALL goals
            if (!goal.isEnabled()) {
                continue;
            }

            if (!goal.getGroups().stream().allMatch(g -> g.canAdd(goal, goalBuilders))) {
                // Group does not permit this goal to be added.
                continue;
            }
/*
            GoalRequirements goalRequirements = goal.getRequirements();
            if (goalRequirements != null) {
                if (!goalRequirements.isTeamsSizeOk(teams.size())) {
                    continue;
                }
                if (!goalRequirements.isSatisfied(biomes, structures)) {
                    continue;
                }
            }*/

            Optional<Goal<?>> constructed = goal.buildGenerated(context);
            if(constructed.isPresent()) {
                goalBuilders.add(goal);
                goals.add(constructed.get());
            }
        }

        // If we didn't get enough goals, try again with a new shuffle
        if (goals.size() < size * size) {
            Lockout.log("Board generation attempt " + (recursionDepth + 1) + ": only got " + goals.size() + " goals, need " + (size * size));
            return generateBoard(size, recursionDepth + 1);
        }

        // Construct the goals with ramdom options
/*        List<Goal> goals = new ArrayList<>();
        for(GoalBuilder<?> goal : goalBuilders) {
            goals.add(goal.buildGenerated());
        }*/

        // Shuffle the board again. Some goals will always be after some other goals (GoalGroup#requirePredecessor),
        // and shuffle fixes this.
        Collections.shuffle(goals);

        return new LockoutBoard(goals);
    }

}

package me.marin.lockout.generator;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.server.game.ServerLockoutBoard;
import me.marin.lockout.server.goal.ServerGoal;

import java.util.*;

public class BoardGenerator {

    private final List<GoalBuilder<?,?>> registeredGoals;
    private final GoalRequirementContext context;
    private static final int MAX_RECURSION_DEPTH = 100;

    public BoardGenerator(List<GoalBuilder<?,?>> registeredGoals, GoalRequirementContext context) {
        this.registeredGoals = registeredGoals;
        this.context = context;
    }

    public ServerLockoutBoard generateBoard(int size) {
        return generateBoard(size, 0);
    }

    private ServerLockoutBoard generateBoard(int size, int recursionDepth) {
        // Prevent infinite recursion
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            Lockout.log("Board generation failed: max recursion depth (" + MAX_RECURSION_DEPTH + ") reached");
            return null;
        }

        Collections.shuffle(registeredGoals);

        List<GoalBuilder<?,?>> goalBuilders = new ArrayList<>();
        List<ServerGoal<?>> goals = new ArrayList<>();

        ListIterator<GoalBuilder<?,?>> it = registeredGoals.listIterator();
        while (goals.size() < size * size && it.hasNext()) {
            GoalBuilder<?,?> goal = it.next();

            // Always check enable state first - this applies to ALL goals
            if (!goal.isEnabled()) {
                continue;
            }

            if (!goal.getGroups().stream().allMatch(g -> g.canAdd(goal, goalBuilders))) {
                // Group does not permit this goal to be added.
                continue;
            }

            var constructed = goal.buildGeneratedServer(context);
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

        Collections.shuffle(goals);

        return new ServerLockoutBoard(goals);
    }

}

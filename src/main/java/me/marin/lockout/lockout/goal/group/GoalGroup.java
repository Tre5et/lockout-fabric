package me.marin.lockout.lockout.goal.group;

import me.marin.lockout.lockout.goal.builder.GoalBuilder;

import java.util.List;

public interface GoalGroup {
    boolean canAdd(GoalBuilder<?,?> goal, List<GoalBuilder<?,?>> existingGoals);

    default List<GoalBuilder<?,?>> goalsMatching(List<GoalBuilder<?,?>> goalBuilders) {
        return goalBuilders.stream().filter(g -> g.getGroups().contains(this)).toList();
    }

    static Limited withLimit(int limit) {
        return new GoalGroup.Limited(limit);
    }

    record Limited(int limit) implements GoalGroup {
        @Override
        public boolean canAdd(GoalBuilder<?,?> goal, List<GoalBuilder<?,?>> existingGoals) {
            return goalsMatching(existingGoals).size() < limit;
        }
    }
}

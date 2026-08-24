package me.marin.lockout.lockout;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: make this an actual {@link net.minecraft.core.Registry}
 */
public class GoalRegistry {

    public static final GoalRegistry INSTANCE = new GoalRegistry();

    private final Map<String, GoalBuilder<?>> registry = new LinkedHashMap<>();

    private GoalRegistry() {}

    public String register(GoalBuilder<?> goalBuilder) {
        if (registry.containsKey(goalBuilder.getStaticId())) {
            Lockout.log("Goal with id " + goalBuilder + " has already been registered.");
            return null;
        }
        registry.put(goalBuilder.getStaticId(), goalBuilder);
        return goalBuilder.getStaticId();
    }

    public boolean isRegistered(String id) {
        return registry.containsKey(id);
    }

    public GoalBuilder<?> get(String id) {
        return registry.get(id);
    }

    public List<GoalBuilder<?>> getRegisteredGoals() {
        return new ArrayList<>(registry.values());
    }

    public Map<String, GoalBuilder<?>> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }

    public List<me.marin.lockout.lockout.goal.Goal> constructGoals(List<Pair<String, String>> goalData) throws IllegalGoalConstructionException {
        List<me.marin.lockout.lockout.goal.Goal> goals = new ArrayList<>();
        List<Pair<Pair<String, String>, Exception>> invalidGoals = new ArrayList<>();
        for(Pair<String, String> goal : goalData) {
            if(!isRegistered(goal.getA())) {
                invalidGoals.add(new Pair<>(goal, new IllegalGoalConstructionException("Goal does not exist")));
            } else {
                try {
                    goals.add(get(goal.getA()).buildFromSerializedData(goal.getB()));
                } catch (IllegalGoalConstructionException e) {
                    invalidGoals.add(new Pair<>(goal, e));
                }
            }
        }
        if(!invalidGoals.isEmpty()) {
            throw new IllegalGoalConstructionException("Failed to construct some goals: " +
                    invalidGoals.stream()
                            .map(g -> g.getA().getA() + " (" + g.getA().getB() + "): " + g.getB().getMessage())
                            .collect(Collectors.joining("; "))
            );
        }
        return goals;
    }
}

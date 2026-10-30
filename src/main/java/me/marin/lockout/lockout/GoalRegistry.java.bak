package me.marin.lockout.lockout;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;

import java.util.*;

/**
 * TODO: make this an actual {@link net.minecraft.core.Registry}
 */
public class GoalRegistry {

    public static final GoalRegistry INSTANCE = new GoalRegistry();

    private final Map<String, GoalBuilder<?,?>> registry = new LinkedHashMap<>();

    private GoalRegistry() {}

    public String register(GoalBuilder<?,?> goalBuilder) {
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

    public GoalBuilder<?,?> get(String id) {
        return registry.get(id);
    }

    public List<GoalBuilder<?,?>> getRegisteredGoals() {
        return new ArrayList<>(registry.values());
    }

    public Map<String, GoalBuilder<?,?>> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }
}

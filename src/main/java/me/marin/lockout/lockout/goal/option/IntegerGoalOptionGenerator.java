package me.marin.lockout.lockout.goal.option;

import lombok.Getter;
import me.marin.lockout.lockout.goal.builder.IllegalGoalConstructionException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class IntegerGoalOptionGenerator implements GoalOptionGenerator<Integer> {
    @Getter
    private final int min;
    @Getter
    private final int max;
    @Getter
    private final int step;
    private final int exampleCount;

    public IntegerGoalOptionGenerator(int min, int max, int step, int exampleCount) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.exampleCount = exampleCount;
    }

    @Override
    public Optional<Integer> generate(Function<Integer, Boolean> allowOption) {
        return RANDOM.ints(min/step, (max+1)/step)
                .distinct()
                .filter(allowOption::apply)
                .map(n -> n*step)
                .boxed()
                .findFirst();
    }

    @Override
    public List<Integer> examples() {
        return RANDOM.ints(min/step, (max+1)/step)
                .distinct()
                .limit(exampleCount)
                .map(n -> n*step)
                .boxed()
                .toList();
    }

    @Override
    public String serialize(Integer option) {
        return String.valueOf(option);
    }

    @Override
    public Integer deserialize(String serialized) throws IllegalGoalConstructionException {
        try {
            return Integer.parseInt(serialized);
        } catch (NumberFormatException e) {
            throw new IllegalGoalConstructionException("Not a valid integer: " + serialized, e);
        }
    }
}

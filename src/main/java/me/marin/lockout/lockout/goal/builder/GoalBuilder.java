package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.generator.GoalRequirements;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class GoalBuilder<T> {
    @Getter
    protected final String id;
    @Getter
    protected final GoalCategory category;
    @Getter
    @Setter
    protected GoalRequirements requirements = null;
    @Getter
    @Setter
    protected TooltipInfo tooltipInfo = null;
    @Getter
    protected final List<GoalGroup> groups = new ArrayList<>();
    @Getter
    @Setter
    protected boolean enabled = true;

    public GoalBuilder(String id, GoalCategory category) {
        this.id = id;
        this.category = category;
    }

    public abstract GoalOptionGenerator<T> optionGenerator();

    public abstract Goal build(T option);

    public Goal buildArbitrary() {
        if(optionGenerator() == null) {
            return build(null);
        }
        return build(optionGenerator().generate());
    }

    public List<Goal> buildExamples() {
        if(optionGenerator() == null) {
            return List.of(build(null));
        }
        return optionGenerator().examples().stream()
                .map(this::build)
                .toList();
    }

    public Goal buildGeneric(Object option) throws IllegalGoalConstructionException {
        if(option == null) return build(null);
        try {
            return build((T)option);
        } catch (ClassCastException e) {
            throw new IllegalGoalConstructionException("Option is not of correct type: " + option, e);
        }
    }

    public Goal buildFromSerializedData(String option) throws IllegalGoalConstructionException {
        if(optionGenerator() == null) {
            return build(null);
        }
        return build(optionGenerator().deserialize(option));
    }

    public String serializeOption(T option) {
        if(optionGenerator() == null || option == null) {
            return null;
        }
        return optionGenerator().serialize(option);
    }

    public GoalBuilder<T> require(GoalRequirements requirements) {
        setRequirements(requirements);
        return this;
    }

    public GoalBuilder<T> tooltip(TooltipInfo tooltipInfo) {
        setTooltipInfo(tooltipInfo);
        return this;
    }

    public GoalBuilder<T> group(GoalGroup group) {
        getGroups().add(group);
        return this;
    }

    public GoalBuilder<T> defaultEnabled(boolean defaultEnabled) {
        setEnabled(defaultEnabled);
        return this;
    }
}

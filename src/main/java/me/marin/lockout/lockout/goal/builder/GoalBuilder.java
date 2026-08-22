package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class GoalBuilder<T> {
    @Getter
    protected final String id;
    @Getter
    protected final GoalCategory category;
    @Getter
    protected List<GoalRequirement<? super T>> requirements = new ArrayList<>();
    @Getter
    @Setter
    protected TooltipInfo tooltipInfo = null;
    @Getter
    protected final List<GoalGroup> groups = new ArrayList<>();
    protected final List<GoalHint> hints = new ArrayList<>();
    @Getter
    @Setter
    protected boolean enabled = true;

    public GoalBuilder(String id, GoalCategory category) {
        this.id = id;
        this.category = category;
    }

    public abstract GoalOptionGenerator<T> optionGenerator();

    public abstract Goal build(T option);

    public Optional<Goal> buildGenerated(GoalRequirementContext context) {
        if(optionGenerator() == null) {
            if(requirements.stream().allMatch(r -> r.optionSatisfiedBy(context, null))) {
                return Optional.of(build(null));
            }
            return Optional.empty();
        }
        Optional<T> option = optionGenerator().generate((o) -> requirements.stream()
                .allMatch(r -> r.optionSatisfiedBy(context, o)));
        return option.map(this::build);
    }

    public List<Goal> buildExamples() {
        if(optionGenerator() == null) {
            return List.of(build(null));
        }
        return optionGenerator().examples().stream()
                .map(this::build)
                .toList();
    }

    @SuppressWarnings("unchecked")
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

    public List<GoalHint> getHints(T option) {
        List<GoalHint> hints = new ArrayList<>(this.hints);
        for(GoalRequirement<? super T> requirement : requirements) {
            hints.addAll(requirement.getHints(option));
        }
        return hints;
    }

    public GoalBuilder<T> require(GoalRequirement<? super T> requirements) {
        getRequirements().add(requirements);
        return this;
    }

    public GoalBuilder<T> tooltip(TooltipInfo tooltipInfo) {
        setTooltipInfo(tooltipInfo);
        return this;
    }

    public GoalBuilder<T> group(GoalGroup... groups) {
        getGroups().addAll(Arrays.stream(groups).toList());
        return this;
    }

    public GoalBuilder<T> defaultEnabled(boolean defaultEnabled) {
        setEnabled(defaultEnabled);
        return this;
    }

    public GoalBuilder<T> hint(GoalHint... hint) {
        hints.addAll(Arrays.stream(hint).toList());
        return this;
    }
}

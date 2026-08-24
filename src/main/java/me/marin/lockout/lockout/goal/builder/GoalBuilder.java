package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.id.IdProvider;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractorProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfo;
import me.marin.lockout.lockout.goal.tooltip.TooltipInfoProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class GoalBuilder<T> {
    protected final String id;
    @Getter
    protected final GoalCategory category;
    @Getter
    protected List<GoalRequirement<? super T>> requirements = new ArrayList<>();
    private IdProvider<T> customIdProvider = null;
    private NameExtractorProvider<T> customNameExtractorProvider = null;
    private TextureExtractorProvider<T> cutomTextureExtractorProvider = null;
    private TooltipInfoProvider<T> customTooltipInfoProvider = null;
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

    public String defaultId(T option) {
        return id;
    }

    public abstract NameExtractor defaultNameExtractor(T option);

    public abstract TextureExtractor defaultTextureExtractor(T option);

    public TooltipInfo defaultTooltipInfo(T option) {
        return null;
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

    public String getStaticId() {
        return id;
    }

    public String getId(T option) {
        if(customIdProvider != null) return customIdProvider.get(option);
        return defaultId(option);
    }

    public NameExtractor getNameExtractor(T option) {
        if(customNameExtractorProvider != null) return customNameExtractorProvider.get(option);
        return defaultNameExtractor(option);
    }

    public TextureExtractor getTextureExtractor(T option) {
        if(cutomTextureExtractorProvider != null) return cutomTextureExtractorProvider.get(option);
        return defaultTextureExtractor(option);
    }

    public Optional<TooltipInfo> getTooltipInfo(T option) {
        if(customTooltipInfoProvider != null) return Optional.ofNullable(customTooltipInfoProvider.get(option));
        return Optional.ofNullable(defaultTooltipInfo(option));
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

    public GoalBuilder<T> customId(IdProvider<T> idProvider) {
        this.customIdProvider = idProvider;
        return this;
    }

    public GoalBuilder<T> customId(String id) {
        return customId(_ -> id);
    }

    public GoalBuilder<T> customNameExtractor(NameExtractorProvider<T> nameExtractorProvider) {
        this.customNameExtractorProvider = nameExtractorProvider;
        return this;
    }

    public GoalBuilder<T> customNameExtractor(NameExtractor extractor) {
        return customNameExtractor(_ -> extractor);
    }

    public GoalBuilder<T> customTextureExtractor(TextureExtractorProvider<T> textureExtractorProvider) {
        this.cutomTextureExtractorProvider = textureExtractorProvider;
        return this;
    }

    public GoalBuilder<T> customTextureExtractor(TextureExtractor extractor) {
        return customTextureExtractor(_ -> extractor);
    }

    public GoalBuilder<T> customTooltip(TooltipInfoProvider<T> tooltipInfoProvider) {
        this.customTooltipInfoProvider = tooltipInfoProvider;
        return this;
    }

    public GoalBuilder<T> customTooltip(TooltipInfo info) {
        return customTooltip(_ -> info);
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

    @Override
    public String toString() {
        return getStaticId();
    }
}

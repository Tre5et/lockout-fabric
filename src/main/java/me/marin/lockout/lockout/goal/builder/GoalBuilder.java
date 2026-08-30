package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.builder.ClientGoalBuildParameters;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.server.goal.ServerGoal;
import me.marin.lockout.server.goal.builder.ServerGoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class GoalBuilder<U,T> {
    protected final String id;
    @Getter
    protected final GoalCategory category;
    @Getter
    protected List<GoalRequirement<? super T>> requirements = new ArrayList<>();
    private IdProvider<T> customIdProvider = null;
    private NameProvider<T> customNameProvider = null;
    private TextureExtractorProvider<T> cutomTextureExtractorProvider = null;
    @Getter
    protected final List<GoalGroup> groups = new ArrayList<>();
    protected final List<HintCombination<?>> hints = new ArrayList<>();
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

    public abstract Component defaultName(T option);

    public abstract TextureExtractor defaultTextureExtractor(T option);

    public Optional<GoalOptionGenerator<T>> getOptionGenerator() {
        return Optional.empty();
    }

    public Optional<ClientGoalOptionGenerator<T>> getClientOptionGenerator() {
        return Optional.empty();
    }

    public abstract ServerGoalProgress<U,?> getServerGoalProgress(T option);

    public abstract ClientGoalProgress<?> getClientGoalProgress(T option);

    public ServerGoal<U> buildServer(T option) {
        return new ServerGoal<>(new ServerGoalBuildParameters<>(
                getId(option),
                getBuildData(option),
                getServerGoalProgress(option),
                getServerHints(option)
        ));
    }

    public ClientGoal buildClient(T option) {
        return new ClientGoal(new ClientGoalBuildParameters(
                getId(option),
                getBuildData(option),
                getName(option),
                getTextureExtractor(option),
                getClientGoalProgress(option),
                getClientHints(option)
        ));
    }

    public <G extends Goal> Optional<G> buildGenerated(GoalRequirementContext context, Function<T,G> builder) {
        Optional<GoalOptionGenerator<T>> optionGenerator = getOptionGenerator();
        if(optionGenerator.isEmpty()) {
            if(requirements.stream().allMatch(r -> r.optionSatisfiedBy(context, null))) {
                return Optional.of(builder.apply(null));
            }
            return Optional.empty();
        }
        Optional<T> option = optionGenerator.get().generate((o) -> requirements.stream()
                .allMatch(r -> r.optionSatisfiedBy(context, o)));
        return option.map(builder);
    }

    public Optional<ServerGoal<U>> buildGeneratedServer(GoalRequirementContext context) {
        return buildGenerated(context, this::buildServer);
    }

    public <G extends Goal> List<G> buildExamples(Function<T,G> builder) {
        Optional<GoalOptionGenerator<T>> optionGenerator = getOptionGenerator();
        return optionGenerator.map(g -> g.examples().stream()
                .map(builder)
                .toList()
        ).orElseGet(() -> List.of(builder.apply(null)));
    }

    public List<ClientGoal> buildClientExamples() {
        return buildExamples(this::buildClient);
    }

    @SuppressWarnings("unchecked")
    public <G extends Goal> G buildGeneric(Object option, Function<T,G> builder) throws IllegalGoalConstructionException {
        if(option == null) return builder.apply(null);
        try {
            return builder.apply((T)option);
        } catch (ClassCastException e) {
            throw new IllegalGoalConstructionException("Option is not of correct type: " + option, e);
        }
    }

    public ClientGoal buildClientGeneric(Object option) throws IllegalGoalConstructionException {
        return buildGeneric(option, this::buildClient);
    }

    public <G extends Goal> G buildFromSerializedData(String option, Function<T,G> builder) throws IllegalGoalConstructionException {
        if(getOptionGenerator().isEmpty()) {
            return builder.apply(null);
        }
        return builder.apply(getOptionGenerator().get().deserialize(option));
    }

    public ServerGoal<U> buildServerFromSerializedData(String option) throws IllegalGoalConstructionException {
        return buildFromSerializedData(option, this::buildServer);
    }

    public ClientGoal buildClientFromSerializedData(String option) throws IllegalGoalConstructionException {
        return buildFromSerializedData(option, this::buildClient);
    }

    public String getStaticId() {
        return id;
    }

    public String getId(T option) {
        if(customIdProvider != null) return customIdProvider.get(option);
        return defaultId(option);
    }

    public Component getName(T option) {
        if(customNameProvider != null) return customNameProvider.get(option);
        return defaultName(option);
    }

    public TextureExtractor getTextureExtractor(T option) {
        if(cutomTextureExtractorProvider != null) return cutomTextureExtractorProvider.get(option);
        return defaultTextureExtractor(option);
    }

    public List<ServerHint<?>> getServerHints(T option) {
        List<ServerHint<?>> hints = new ArrayList<>(this.hints.stream().map(HintCombination::constructServer).toList());
        for(GoalRequirement<? super T> requirement : requirements) {
            hints.addAll(requirement.getServerHints(option));
        }
        return hints;
    }

    public List<ClientHint<?>> getClientHints(T option) {
        List<ClientHint<?>> hints = new ArrayList<>(this.hints.stream().map(HintCombination::constructClient).toList());
        for(GoalRequirement<? super T> requirement : requirements) {
            hints.addAll(requirement.getClientHints(option));
        }
        return hints;
    }

    public BuildData getBuildData(T option) {
        if(option == null || getOptionGenerator().isEmpty()) {
            return new BuildData(getStaticId(), Optional.empty());
        }
        return new BuildData(getStaticId(), Optional.ofNullable(getOptionGenerator().get().serialize(option)));
    }

    public GoalBuilder<U,T> require(GoalRequirement<? super T> requirements) {
        getRequirements().add(requirements);
        return this;
    }

    public GoalBuilder<U,T> customId(IdProvider<T> idProvider) {
        this.customIdProvider = idProvider;
        return this;
    }

    public GoalBuilder<U,T> customId(String id) {
        return customId(_ -> id);
    }

    public GoalBuilder<U,T> customName(NameProvider<T> nameExtractorProvider) {
        this.customNameProvider = nameExtractorProvider;
        return this;
    }

    public GoalBuilder<U,T> customName(NameProvider.StringNameProvider<T> nameExtractorProvider) {
        this.customNameProvider = nameExtractorProvider;
        return this;
    }

    public GoalBuilder<U,T> customTextureExtractor(TextureExtractorProvider<T> textureExtractorProvider) {
        this.cutomTextureExtractorProvider = textureExtractorProvider;
        return this;
    }

    public GoalBuilder<U,T> customTextureExtractor(TextureExtractor extractor) {
        return customTextureExtractor(_ -> extractor);
    }

    public GoalBuilder<U,T> group(GoalGroup... groups) {
        getGroups().addAll(Arrays.stream(groups).toList());
        return this;
    }

    public GoalBuilder<U,T> defaultEnabled(boolean defaultEnabled) {
        setEnabled(defaultEnabled);
        return this;
    }

    public GoalBuilder<U,T> hint(HintCombination<?>... hint) {
        hints.addAll(Arrays.stream(hint).toList());
        return this;
    }

    @Override
    public String toString() {
        return getStaticId();
    }
}

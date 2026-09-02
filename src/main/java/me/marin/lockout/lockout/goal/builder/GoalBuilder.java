package me.marin.lockout.lockout.goal.builder;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.builder.ClientGoalBuildParameters;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.id.IdProvider;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameProvider;
import me.marin.lockout.lockout.goal.rendering.name.StringNameProvider;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractorProvider;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.server.goal.ServerGoal;
import me.marin.lockout.server.goal.builder.ServerGoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class GoalBuilder<U,T> {
    protected final String idPrefix;
    protected final String namePrefix;
    @Getter
    protected final GoalCategory category;
    @Getter
    protected final GoalOptionSupplier<T> optionSupplier;
    @Getter
    protected final GoalProgressSupplier<T,U,?> progressSupplier;
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

    public GoalBuilder(String idPrefix, String namePrefix, GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T,U,?> progressSupplier) {
        this.idPrefix = idPrefix;
        this.namePrefix = namePrefix;
        this.category = category;
        this.optionSupplier = optionSupplier;
        this.progressSupplier = progressSupplier;
    }

    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return textureExtractor;
    }

    /**
     * This method should be overridden by a class with a fixed U type and should stay blank.
     */
    public abstract void reifiedUpdater(U update);

    public ServerGoal<U> buildServer(T option) {
        return new ServerGoal<>(new ServerGoalBuildParameters<>(
                getId(option),
                getBuildData(option),
                progressSupplier.getServer(option),
                getServerHints(option),
                this::reifiedUpdater
        ));
    }

    public ClientGoal buildClient(T option) {
        return new ClientGoal(new ClientGoalBuildParameters(
                getId(option),
                getBuildData(option),
                getName(option),
                getTextureExtractor(option),
                progressSupplier.getClient(option),
                getClientHints(option)
        ));
    }

    public <G extends Goal> Optional<G> buildGenerated(GoalRequirementContext context, Function<T,G> builder) {
        GoalOptionGenerator<T> optionGenerator = optionSupplier.get();
        if(optionGenerator == null) {
            if(requirements.stream().allMatch(r -> r.optionSatisfiedBy(context, null))) {
                return Optional.of(builder.apply(null));
            }
            return Optional.empty();
        }
        Optional<T> option = optionGenerator.generate((o) -> requirements.stream()
                .allMatch(r -> r.optionSatisfiedBy(context, o)));
        return option.map(builder);
    }

    public Optional<ServerGoal<U>> buildGeneratedServer(GoalRequirementContext context) {
        return buildGenerated(context, this::buildServer);
    }

    public <G extends Goal> List<G> buildExamples(Function<T,G> builder) {
        GoalOptionGenerator<T> optionGenerator = optionSupplier.get();
        if(optionGenerator == null) return List.of(builder.apply(null));
        return optionGenerator.examples().stream()
                .map(builder)
                .toList();
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
            throw new IllegalGoalConstructionException("Option is not of correct entity: " + option, e);
        }
    }

    public ClientGoal buildClientGeneric(Object option) throws IllegalGoalConstructionException {
        return buildGeneric(option, this::buildClient);
    }

    public <G extends Goal> G buildFromSerializedData(String option, Function<T,G> builder) throws IllegalGoalConstructionException {
        GoalOptionGenerator<T> optionGenerator = optionSupplier.get();
        if(optionGenerator == null) {
            return builder.apply(null);
        }
        return builder.apply(optionGenerator.deserialize(option));
    }

    public ServerGoal<U> buildServerFromSerializedData(String option) throws IllegalGoalConstructionException {
        return buildFromSerializedData(option, this::buildServer);
    }

    public ClientGoal buildClientFromSerializedData(String option) throws IllegalGoalConstructionException {
        return buildFromSerializedData(option, this::buildClient);
    }

    public String getStaticId() {
        return formatId(idPrefix + "_" + optionSupplier.getStaticId() + "_" + progressSupplier.getStaticId());
    }

    public String getId(T option) {
        if(customIdProvider != null) return customIdProvider.get(option);
        return formatId(idPrefix + "_" + progressSupplier.getId(option));
    }

    public Component getName(T option) {
        if(customNameProvider != null) return customNameProvider.get(option);
        return Component.literal(namePrefix + " " + progressSupplier.getName(option));
    }

    public TextureExtractor getTextureExtractor(T option) {
        if(cutomTextureExtractorProvider != null) return cutomTextureExtractorProvider.get(option);
        return progressSupplier.applyFinalTextureExtractor(applyTextureExtractor(progressSupplier.getTextureExtractor(option), option), option);
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
        if(option == null) {
            return new BuildData(getStaticId(), Optional.empty());
        }
        return new BuildData(getStaticId(), Optional.ofNullable(optionSupplier.get().serialize(option)));
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

    public GoalBuilder<U,T> customName(StringNameProvider<T> nameExtractorProvider) {
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

    public static String formatId(String string) {
        return string.toUpperCase().replaceAll("__+", "_");
    }
}

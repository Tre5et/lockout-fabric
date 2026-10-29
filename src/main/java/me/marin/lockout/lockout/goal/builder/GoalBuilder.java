package me.marin.lockout.lockout.goal.builder;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.client.goal.builder.ClientGoalBuildParameters;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.block.MineBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.block.UseItemOnBlockGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DealDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.DeathGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.KillEntityGoalBuilder;
import me.marin.lockout.lockout.goal.builder.damage.TakeDamageGoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.*;
import me.marin.lockout.lockout.goal.builder.experience.ReachExperienceLevelGoalBuilder;
import me.marin.lockout.lockout.goal.builder.inventory.UpdateInventoryGoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.*;
import me.marin.lockout.lockout.goal.builder.miscellanious.*;
import me.marin.lockout.lockout.goal.builder.statistic.ChangeStatisticGoalBuilder;
import me.marin.lockout.lockout.goal.builder.statistic.HaveStatusEffectGoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
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
import me.marin.lockout.registries.LockoutRegistries;
import me.marin.lockout.registries.LockoutRegistryKeys;
import me.marin.lockout.server.goal.ServerGoal;
import me.marin.lockout.server.goal.builder.ServerGoalBuildParameters;
import me.marin.lockout.server.goal.hint.ServerHint;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;

public abstract class GoalBuilder<U,T extends Object> {
    public static final Codec<GoalBuilder<?,?>> CODEC = LockoutRegistries.GOAL_BUILDER_TYPE.byNameCodec().dispatch(GoalBuilder::codec, c -> c);

    public static <T extends GoalBuilder<?,?>> Products.P11<RecordCodecBuilder.Mu<T>, String, String, Holder<GoalCategory>, GoalDifficultySupplier<?>, HolderSet<GoalRequirement>, Optional<IdProvider>, Optional<NameProvider>, Optional<TextureExtractorProvider>, HolderSet<GoalGroup>, List<HintCombination<?>>, Boolean> codecBuilder(String defaultIdPrefix, String defaultNamePrefix, RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.STRING.optionalFieldOf("id_prefix", defaultIdPrefix).forGetter(GoalBuilder::getIdPrefix),
                Codec.STRING.optionalFieldOf("name_prefix", defaultNamePrefix).forGetter(GoalBuilder::getNamePrefix),
                RegistryCodecs.holder(LockoutRegistryKeys.GOAL_CATEGORY).fieldOf("category").forGetter(GoalBuilder::getCategory),
                GoalDifficultySupplier.CODEC.fieldOf("difficulty").forGetter(GoalBuilder::getDifficultySupplier),
                RegistryCodecs.holderSet(LockoutRegistryKeys.GOAL_REQUIREMENT).optionalFieldOf("requirements", HolderSet.empty()).forGetter(GoalBuilder::getRequirements),
                IdProvider.CODEC.optionalFieldOf("id").forGetter((b) -> Optional.ofNullable(b.getCustomIdProvider())),
                NameProvider.CODEC.optionalFieldOf("name").forGetter((b) -> Optional.ofNullable(b.getCustomNameProvider())),
                TextureExtractorProvider.CODEC.optionalFieldOf("texture").forGetter((b) -> Optional.ofNullable(b.getCustomTextureExtractorProvider())),
                RegistryCodecs.holderSet(LockoutRegistryKeys.GOAL_GROUP).optionalFieldOf("groups", HolderSet.empty()).forGetter(GoalBuilder::getGroups),
                Codec.list(HintCombination.CODEC).optionalFieldOf("hints", List.of()).forGetter(GoalBuilder::getHints),
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(GoalBuilder::isEnabled)
        );
    }

    public static <T extends NumericRange> Products.P3<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer> numericCodec(int defaultStep, RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.INT.fieldOf("min").forGetter(NumericRange::getMin),
                Codec.INT.fieldOf("max").forGetter(NumericRange::getMax),
                Codec.INT.optionalFieldOf("step", defaultStep).forGetter(NumericRange::getStep)
        );
    }

    @Getter
    protected final String idPrefix;
    @Getter
    protected final String namePrefix;
    @Getter
    protected final Holder<GoalCategory> category;
    @Getter
    protected final GoalDifficultySupplier<?> difficultySupplier;
    @Getter
    protected final GoalOptionSupplier<T> optionSupplier;
    @Getter
    protected final GoalProgressSupplier<T,U,?> progressSupplier;
    @Getter
    protected HolderSet<GoalRequirement> requirements = HolderSet.empty();
    @Getter
    private IdProvider customIdProvider = null;
    @Getter
    private NameProvider customNameProvider = null;
    @Getter
    private TextureExtractorProvider customTextureExtractorProvider = null;
    @Getter
    protected HolderSet<GoalGroup> groups = HolderSet.empty();
    @Getter
    protected List<HintCombination<?>> hints = new ArrayList<>();
    @Getter
    @Setter
    protected boolean enabled = true;

    public GoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, U, ?> progressSupplier, String idPrefix, String namePrefix, Holder<GoalCategory> category, GoalDifficultySupplier<?> difficultySupplier, HolderSet<GoalRequirement> requirements, Optional<IdProvider> customIdProvider, Optional<NameProvider> customNameProvider, Optional<TextureExtractorProvider> customTextureExtractorProvider, HolderSet<GoalGroup> groups, List<HintCombination<?>> hints, boolean enabled) {
        this.idPrefix = idPrefix;
        this.namePrefix = namePrefix;
        this.category = category;
        this.difficultySupplier = difficultySupplier;
        this.optionSupplier = optionSupplier;
        this.progressSupplier = progressSupplier;
        this.requirements = requirements;
        this.customIdProvider = customIdProvider.orElse(null);
        this.customNameProvider = customNameProvider.orElse(null);
        this.customTextureExtractorProvider = customTextureExtractorProvider.orElse(null);
        this.groups = groups;
        this.hints = hints;
        this.enabled = enabled;
    }

    public abstract MapCodec<? extends GoalBuilder<?,?>> codec();

    public static void bootstrap(Registry<MapCodec<? extends GoalBuilder<?,?>>> registry) {
        LockoutRegistries.register(registry, "mine_block/any", MineBlockGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "mine_block/unique", MineBlockGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "use_item_on_block/any", UseItemOnBlockGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "deal_damage/total", DealDamageGoalBuilder.Total.MAP_CODEC);
        LockoutRegistries.register(registry, "death/damage_type", DeathGoalBuilder.Type.MAP_CODEC);
        LockoutRegistries.register(registry, "death/entity", DeathGoalBuilder.Entity.MAP_CODEC);
        LockoutRegistries.register(registry, "death/fall", DeathGoalBuilder.FallOffBlock.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/any", KillEntityGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/unique", KillEntityGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/total", KillEntityGoalBuilder.Total.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/colored_sheep", KillEntityGoalBuilder.ColoredSheep.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/method", KillEntityGoalBuilder.DeathType.MAP_CODEC);
        LockoutRegistries.register(registry, "kill/dimension", KillEntityGoalBuilder.Dimension.MAP_CODEC);
        LockoutRegistries.register(registry, "take_damage/total", TakeDamageGoalBuilder.Total.MAP_CODEC);
        LockoutRegistries.register(registry, "age_lock/any", AgeLockMobGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "anger/any", AngerMobGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "breed/any", BreedAnimalGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "breed/unique", BreedAnimalGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "leash/any", LeashEntityGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "leash/unique", LeashEntityGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "spawn/any", SpawnEntityGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "use_item_on_entity/exactly", UseItemOnEntityGoalBuilder.Exactly.MAP_CODEC);
        LockoutRegistries.register(registry, "use_item_on_entity/name_tag", UseItemOnEntityGoalBuilder.NameTag.MAP_CODEC);
        LockoutRegistries.register(registry, "use_item_on_entity/unique", UseItemOnEntityGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "experience/total", ReachExperienceLevelGoalBuilder.Total.MAP_CODEC);
        LockoutRegistries.register(registry, "inventory/fill_block", UpdateInventoryGoalBuilder.FillBlock.MAP_CODEC);
        LockoutRegistries.register(registry, "inventory/fill_entity", UpdateInventoryGoalBuilder.FillEntity.MAP_CODEC);
        LockoutRegistries.register(registry, "block/have_disabled", BlockWithItemGoalBuilder.HaveDisabled.MAP_CODEC);
        LockoutRegistries.register(registry, "break/any", BreakItemGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "break/component", BreakItemGoalBuilder.Component.MAP_CODEC);
        LockoutRegistries.register(registry, "brew/effect", BrewPotionGoalBuilder.Effect.MAP_CODEC);
        LockoutRegistries.register(registry, "brew/type", BrewPotionGoalBuilder.Type.MAP_CODEC);
        LockoutRegistries.register(registry, "compost/unique_with_component", CompostItemsGoalBuilder.UniqueWithComponent.MAP_CODEC);
        LockoutRegistries.register(registry, "consume/any", ConsumeItemGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "consume/unique", ConsumeItemGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "consume/unique_with_components", ConsumeItemGoalBuilder.UniqueWithComponents.MAP_CODEC);
        LockoutRegistries.register(registry, "consume/with_status_effect", ConsumeItemGoalBuilder.WithStatusEffect.MAP_CODEC);
        LockoutRegistries.register(registry, "craft/any", CraftItemGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "craft/unique", CraftItemGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/all", ObtainItemGoalBuilder.All.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/any", ObtainItemGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/at_least", ObtainItemGoalBuilder.AtLeast.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/count", ObtainItemGoalBuilder.WithCount.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_stack", ObtainItemGoalBuilder.AnyFullStack.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/colored", ObtainItemGoalBuilder.Colored.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/shield_with_banner", ObtainItemGoalBuilder.ShieldWithBanner.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_inventory_distinct", ObtainItemGoalBuilder.AllDistinct.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/armor_piece", ObtainItemGoalBuilder.ArmorPiece.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/dyed_armor_piece", ObtainItemGoalBuilder.DyedArmorPiece.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_armor_of_material", ObtainItemGoalBuilder.FullArmorOfMaterial.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_enchanted_armor", ObtainItemGoalBuilder.AllEnchantedArmor.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_armor_of_different_material", ObtainItemGoalBuilder.AllDifferentArmorMaterial.MAP_CODEC);
        LockoutRegistries.register(registry, "obtain/full_diffent_dyed_leather_armor", ObtainItemGoalBuilder.AllDifferentDyedLeatherArmor.MAP_CODEC);
        LockoutRegistries.register(registry, "custom/exactly", CustomEventGoalBuilder.Exactly.MAP_CODEC);
        LockoutRegistries.register(registry, "advancement/any", ObtainAdvancementGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "advancement/unique", ObtainAdvancementGoalBuilder.Unique.MAP_CODEC);
        LockoutRegistries.register(registry, "player/empty_hunger", PlayerStateGoalBuilder.EmptyHungerBar.MAP_CODEC);
        LockoutRegistries.register(registry, "player/height", PlayerStateGoalBuilder.HeightAbove.MAP_CODEC);
        LockoutRegistries.register(registry, "player/sleep", PlayerStateGoalBuilder.SleepIn.MAP_CODEC);
        LockoutRegistries.register(registry, "ride/any", RideEntityGoalBuilder.Any.MAP_CODEC);
        LockoutRegistries.register(registry, "statistic/custom", ChangeStatisticGoalBuilder.Custom.MAP_CODEC);
        LockoutRegistries.register(registry, "statistic/custom_total", ChangeStatisticGoalBuilder.CustomTotal.MAP_CODEC);
        LockoutRegistries.register(registry, "statistic/item", ChangeStatisticGoalBuilder.ItemInteraction.MAP_CODEC);
        LockoutRegistries.register(registry, "statistic/item_while_riding", ChangeStatisticGoalBuilder.ItemInteractionWhileRiding.MAP_CODEC);
        LockoutRegistries.register(registry, "status_effect/any", HaveStatusEffectGoalBuilder.Any.MAP_CODEC);
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
                this::reifiedUpdater,
                difficultySupplier.get(option)
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
            if(requirements.stream().allMatch(r -> r.value().optionSatisfiedBy(context, null))) {
                return Optional.of(builder.apply(null));
            }
            return Optional.empty();
        }
        Optional<T> option = optionGenerator.generate((o) -> requirements.stream()
                .allMatch(r -> r.value().optionSatisfiedBy(context, o)));
        if(option.isEmpty()) return Optional.empty();
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
        if(customIdProvider != null) return customIdProvider.get(null);
        return formatId(idPrefix + "_" + optionSupplier.getStaticId() + "_" + progressSupplier.getStaticId());
    }

    public String getId(T option) {
        if(customIdProvider != null) return customIdProvider.get(option);
        return formatId(idPrefix + "_" + progressSupplier.getId(option));
    }

    public Component getName(T option) {
        if(customNameProvider != null) return customNameProvider.get(option);
        return Component.literal(formatName(namePrefix + " " + progressSupplier.getName(option)));
    }

    public TextureExtractor getTextureExtractor(T option) {
        if(customTextureExtractorProvider != null) return customTextureExtractorProvider.get(option);
        return progressSupplier.applyFinalTextureExtractor(applyTextureExtractor(progressSupplier.getTextureExtractor(option), option), option);
    }

    public List<ServerHint<?>> getServerHints(T option) {
        List<ServerHint<?>> hints = new ArrayList<>(this.hints.stream().map(HintCombination::constructServer).toList());
        for(Holder<GoalRequirement> requirement : requirements) {
            hints.addAll(requirement.value().getServerHints(option));
        }
        return hints;
    }

    public List<ClientHint<?>> getClientHints(T option) {
        List<ClientHint<?>> hints = new ArrayList<>(this.hints.stream().map(HintCombination::constructClient).toList());
        for(Holder<GoalRequirement> requirement : requirements) {
            hints.addAll(requirement.value().getClientHints(option));
        }
        return hints;
    }

    public BuildData getBuildData(T option) {
        if(option == null) {
            return new BuildData(getStaticId(), Optional.empty());
        }
        return new BuildData(getStaticId(), Optional.ofNullable(optionSupplier.get().serialize(option)));
    }

/*    public GoalBuilder<U,T> require(Holder<GoalRequirement> requirements) {
        requirements = HolderSet.empty()..add(requirements);
        return this;
    }*/

    public GoalBuilder<U,T> customId(IdProvider idProvider) {
        this.customIdProvider = idProvider;
        return this;
    }

    public GoalBuilder<U,T> customId(String id) {
        return customId(_ -> id);
    }

    public GoalBuilder<U,T> customName(NameProvider nameExtractorProvider) {
        this.customNameProvider = nameExtractorProvider;
        return this;
    }

    public GoalBuilder<U,T> customName(StringNameProvider nameExtractorProvider) {
        this.customNameProvider = nameExtractorProvider;
        return this;
    }

    public GoalBuilder<U,T> customTextureExtractor(TextureExtractorProvider textureExtractorProvider) {
        this.customTextureExtractorProvider = textureExtractorProvider;
        return this;
    }

/*    public GoalBuilder<U,T> group(GoalGroup... groups) {
        getGroups().addAll(Arrays.stream(groups).toList());
        return this;
    }*/

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
        return string.toUpperCase().replaceAll("__+", "_").replaceAll("^_+", "").replaceAll("_+$", "");
    }

    public static String formatName(String string) {
        return string.replaceAll("  +", " ").replaceAll("^ +", "").replaceAll(" +$", "");
    }
}

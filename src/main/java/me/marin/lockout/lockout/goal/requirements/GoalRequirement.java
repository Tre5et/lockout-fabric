package me.marin.lockout.lockout.goal.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.client.goal.hint.PositionClientHint;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.registries.LockoutRegistries;
import me.marin.lockout.server.goal.hint.BiomeServerHint;
import me.marin.lockout.server.goal.hint.ServerHint;
import me.marin.lockout.server.goal.hint.StructureServerHint;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GoalRequirement {
    public static final Codec<GoalRequirement> CODEC = LockoutRegistries.GOAL_REQUIREMENT_TYPE.byNameCodec().dispatch(GoalRequirement::codec, c -> c);

    public abstract MapCodec<? extends GoalRequirement> codec();

    @Getter
    private final GoalRequirementContextInitializer initializer;
    @Getter
    private final List<HintCombination<?>> hints = new ArrayList<>();

    public GoalRequirement(GoalRequirementContextInitializer initializer) {
        this.initializer = initializer;
    }

    public abstract boolean optionSatisfiedBy(GoalRequirementContext context, Object option);

    public abstract List<HintCombination<?>> getHints(Object option);

    public List<ServerHint<?>> getServerHints(Object option) {
        return getHints(option).stream().map(HintCombination::constructServer).collect(Collectors.toUnmodifiableList());
    }

    public List<ClientHint<?>> getClientHints(Object option) {
        return getHints(option).stream().map(HintCombination::constructClient).collect(Collectors.toUnmodifiableList());
    }

    public GoalRequirement withHint(HintCombination<?>... hint) {
        hints.addAll(Arrays.stream(hint).toList());
        return this;
    }

    @SafeVarargs
    public final <N> LimitedToOptions forOptions(N... options) {
        return new LimitedToOptions(Arrays.stream(options).map(Object::toString).toList(), this);
    }

    public OrCombined or(GoalRequirement requirement) {
        return new OrCombined(List.of(this, requirement));
    }

    public AndCombined and(GoalRequirement requirement) {
        return new AndCombined(List.of(this, requirement));
    }

    public ExactlyCombined xor(GoalRequirement requirement) {
        return new ExactlyCombined(List.of(this, requirement), 1);
    }

    public static void bootstrap(Registry<MapCodec<? extends GoalRequirement>> registry) {
        LockoutRegistries.register(registry, "biome", AnyBiome.MAP_CODEC);
        LockoutRegistries.register(registry, "structure", AnyStructure.MAP_CODEC);
        LockoutRegistries.register(registry, "min_teams", TeamCountMin.MAP_CODEC);
        LockoutRegistries.register(registry, "max_teams", TeamCountMax.MAP_CODEC);
        LockoutRegistries.register(registry, "option_limited", LimitedToOptions.MAP_CODEC);
        LockoutRegistries.register(registry, "and", AndCombined.MAP_CODEC);
        LockoutRegistries.register(registry, "or", OrCombined.MAP_CODEC);
        LockoutRegistries.register(registry, "xor", ExactlyCombined.MAP_CODEC);
    }

    public static class AnyBiome extends GoalRequirement {
        public static final MapCodec<AnyBiome> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(AnyBiome::getName),
                MapDecorationType.CODEC.fieldOf("decoration").forGetter(AnyBiome::getDecoration),
                Codec.list(ResourceKey.codec(Registries.BIOME)).fieldOf("biomes").forGetter(AnyBiome::getBiomes)
        ).apply(instance, AnyBiome::new));

        @Getter
        private final String name;
        @Getter
        private final Holder<MapDecorationType> decoration;
        @Getter
        private final List<ResourceKey<Biome>> biomes;

        public AnyBiome(String name, Holder<MapDecorationType> decoration, List<ResourceKey<Biome>> biomes) {
            super(new GoalRequirementContextInitializer.Biomes(biomes));
            this.name = name;
            this.decoration = decoration;
            this.biomes = biomes;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return biomes.stream().anyMatch(b -> context.biomes().get(b).wasLocated());
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return List.of(new HintCombination<>(
                    () -> new BiomeServerHint(name, decoration, List.of(Level.OVERWORLD), biomes),
                    () -> new PositionClientHint(name)
            ));
        }
    }

    public static class AnyStructure extends GoalRequirement {
        public static final MapCodec<AnyStructure> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(AnyStructure::getName),
                MapDecorationType.CODEC.fieldOf("decoration").forGetter(AnyStructure::getDecoration),
                Codec.list(ResourceKey.codec(Registries.STRUCTURE)).fieldOf("structures").forGetter(AnyStructure::getStructures)
        ).apply(instance, AnyStructure::new));

        @Getter
        private final String name;
        @Getter
        private final Holder<MapDecorationType> decoration;
        @Getter
        private final List<ResourceKey<Structure>> structures;

        public AnyStructure(String name, Holder<MapDecorationType> decoration, List<ResourceKey<Structure>> structures) {
            super(new GoalRequirementContextInitializer.Structures(structures));
            this.name = name;
            this.decoration = decoration;
            this.structures = structures;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return structures.stream().anyMatch(s -> context.structures().get(s).wasLocated());
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return List.of(new HintCombination<>(
                    () -> new StructureServerHint(name, decoration, List.of(Level.OVERWORLD), structures),
                    () -> new PositionClientHint(name)
            ));
        }
    }

    public static class LimitedToOptions extends GoalRequirement {
        public static final MapCodec<LimitedToOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(Codec.STRING).fieldOf("options").forGetter(LimitedToOptions::getOptions),
                GoalRequirement.CODEC.fieldOf("requirement").forGetter(LimitedToOptions::getRequirement)
        ).apply(instance, LimitedToOptions::new));

        @Getter
        private final List<String> options;
        @Getter
        private final GoalRequirement requirement;

        public LimitedToOptions(List<String> options, GoalRequirement requirement) {
            super(requirement.initializer);
            this.options = options;
            this.requirement = requirement;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return !options.contains(option.toString()) || requirement.optionSatisfiedBy(context, option);
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            if(options.contains(option.toString())) return requirement.getHints(option);
            return List.of();
        }
    }

    public static class TeamCountMin extends GoalRequirement {
        public static final MapCodec<TeamCountMin> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("count").forGetter(TeamCountMin::getCount)
        ).apply(instance, TeamCountMin::new));

        @Getter
        private final int count;

        public TeamCountMin(int count) {
            super(new GoalRequirementContextInitializer.None());
            this.count = count;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return context.teams().size() >= count;
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return List.of();
        }
    }

    public static class TeamCountMax extends GoalRequirement {
        public static final MapCodec<TeamCountMax> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("count").forGetter(TeamCountMax::getCount)
        ).apply(instance, TeamCountMax::new));

        @Getter
        private final int count;

        public TeamCountMax(int count) {
            super(new GoalRequirementContextInitializer.None());
            this.count = count;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return context.teams().size() <= count;
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return List.of();
        }
    }

    public static abstract class Combinatorial extends GoalRequirement {
        @Getter
        protected final List<GoalRequirement> requirements;

        public Combinatorial(List<GoalRequirement> requirements) {
            super(new GoalRequirementContextInitializer.Combined(requirements.stream().map(GoalRequirement::getInitializer).toList()));
            this.requirements = requirements;
            getHints().addAll(requirements.stream().flatMap(r -> r.getHints().stream()).toList());
        }
    }

    public static class OrCombined extends Combinatorial {
        public static final MapCodec<OrCombined> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(GoalRequirement.CODEC).fieldOf("requirements").forGetter(OrCombined::getRequirements)
        ).apply(instance, OrCombined::new));

        public OrCombined(List<GoalRequirement> goalRequirements) {
            super(goalRequirements);
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return requirements.stream().anyMatch(r -> r.optionSatisfiedBy(context, option));
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return requirements.stream().flatMap(r -> r.getHints(option).stream()).toList();
        }
    }

    public static class AndCombined extends Combinatorial {
        public static final MapCodec<AndCombined> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(GoalRequirement.CODEC).fieldOf("requirements").forGetter(AndCombined::getRequirements)
        ).apply(instance, AndCombined::new));

        public AndCombined(List<GoalRequirement> goalRequirements) {
            super(goalRequirements);
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return requirements.stream().allMatch(r -> r.optionSatisfiedBy(context, option));
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return requirements.stream().flatMap(r -> r.getHints(option).stream()).toList();
        }
    }

    public static class ExactlyCombined extends Combinatorial {
        public static final MapCodec<ExactlyCombined> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(GoalRequirement.CODEC).fieldOf("requirements").forGetter(ExactlyCombined::getRequirements),
                Codec.INT.optionalFieldOf("number", 1).forGetter(ExactlyCombined::getNumber)
        ).apply(instance, ExactlyCombined::new));

        @Getter
        private final int number;

        public ExactlyCombined(List<GoalRequirement> goalRequirements, int number) {
            super(goalRequirements);
            this.number = number;
        }

        @Override
        public MapCodec<? extends GoalRequirement> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return requirements.stream().filter(r -> r.optionSatisfiedBy(context, option)).count() == number;
        }

        @Override
        public List<HintCombination<?>> getHints(Object option) {
            return requirements.stream().flatMap(r -> r.getHints(option).stream()).toList();
        }
    }
}

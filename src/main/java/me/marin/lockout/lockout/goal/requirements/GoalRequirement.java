package me.marin.lockout.lockout.goal.requirements;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GoalRequirement<T> {
    @Getter
    private final GoalRequirementContextInitializer initializer;

    public GoalRequirement(GoalRequirementContextInitializer initializer) {
        this.initializer = initializer;
    }

    public abstract boolean optionSatisfiedBy(GoalRequirementContext context, T option);

    @SafeVarargs
    public final <N extends T> LimitedToOptions<N> forOptions(N... options) {
        return new LimitedToOptions<>(Arrays.stream(options).toList(), this);
    }

    public OrCombined<T> or(GoalRequirement<? super T> requirement) {
        return new OrCombined<>(List.of(this, requirement));
    }

    public AndCombined<T> and(GoalRequirement<? super T> requirement) {
        return new AndCombined<>(List.of(this, requirement));
    }

    public ExactlyCombined<T> xor(GoalRequirement<? super T> requirement) {
        return new ExactlyCombined<>(List.of(this, requirement), 1);
    }

    public static class AllBiomes extends GoalRequirement<Object> {
        private final List<ResourceKey<Biome>> biomes;

        public AllBiomes(List<ResourceKey<Biome>> biomes) {
            super(new GoalRequirementContextInitializer.Biomes(biomes));
            this.biomes = biomes;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return biomes.stream().allMatch(s -> context.structures().get(s).wasLocated());
        }

        @SafeVarargs
        public final AllBiomes alsoRequire(ResourceKey<Biome>... biomes) {
            List<ResourceKey<Biome>> newBiomes = new ArrayList<>(this.biomes);
            newBiomes.addAll(Arrays.stream(biomes).toList());
            return new AllBiomes(newBiomes);
        }
    }

    public static class AnyBiome extends GoalRequirement<Object> {
        private final List<ResourceKey<Biome>> biomes;

        public AnyBiome(List<ResourceKey<Biome>> biomes) {
            super(new GoalRequirementContextInitializer.Biomes(biomes));
            this.biomes = biomes;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return biomes.stream().anyMatch(b -> context.biomes().get(b).wasLocated());
        }

        @SafeVarargs
        public final AnyBiome alsoAllow(ResourceKey<Biome>... biomes) {
            List<ResourceKey<Biome>> newBiomes = new ArrayList<>(this.biomes);
            newBiomes.addAll(Arrays.stream(biomes).toList());
            return new AnyBiome(newBiomes);
        }
    }

    public static class AllStructures extends GoalRequirement<Object> {
        private final List<ResourceKey<Structure>> structures;

        public AllStructures(List<ResourceKey<Structure>> structures) {
            super(new GoalRequirementContextInitializer.Structures(structures));
            this.structures = structures;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return structures.stream().allMatch(s -> context.structures().get(s).wasLocated());
        }

        @SafeVarargs
        public final AllStructures alsoRequire(ResourceKey<Structure>... structures) {
            List<ResourceKey<Structure>> newStructures = new ArrayList<>(this.structures);
            newStructures.addAll(Arrays.stream(structures).toList());
            return new AllStructures(newStructures);
        }
    }

    public static class AnyStructure extends GoalRequirement<Object> {
        private final List<ResourceKey<Structure>> structures;

        public AnyStructure(List<ResourceKey<Structure>> structures) {
            super(new GoalRequirementContextInitializer.Structures(structures));
            this.structures = structures;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return structures.stream().anyMatch(s -> context.structures().get(s).wasLocated());
        }

        @SafeVarargs
        public final AnyStructure alsoAllow(ResourceKey<Structure>... structures) {
            List<ResourceKey<Structure>> newStructures = new ArrayList<>(this.structures);
            newStructures.addAll(Arrays.stream(structures).toList());
            return new AnyStructure(newStructures);
        }
    }

    public static class LimitedToOptions<T> extends GoalRequirement<T> {
        private final List<T> options;
        private final GoalRequirement<? super T> requirement;

        public LimitedToOptions(List<T> options, GoalRequirement<? super T> requirement) {
            super(requirement.initializer);
            this.options = options;
            this.requirement = requirement;
        }


        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, T option) {
            return !options.contains(option) || requirement.optionSatisfiedBy(context, option);
        }
    }

    public static class TeamCountMin extends GoalRequirement<Object> {
        private final int count;

        public TeamCountMin(int count) {
            super(new GoalRequirementContextInitializer.None());
            this.count = count;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return context.teams().size() >= count;
        }
    }

    public static class TeamCountMax extends GoalRequirement<Object> {
        private final int count;

        public TeamCountMax(int count) {
            super(new GoalRequirementContextInitializer.None());
            this.count = count;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, Object option) {
            return context.teams().size() <= count;
        }
    }

    public static abstract class Combinatorial<T> extends GoalRequirement<T> {
        protected final List<GoalRequirement<? super T>> requirements;

        public Combinatorial(List<GoalRequirement<? super T>> requirements) {
            super(new GoalRequirementContextInitializer.Combined(requirements.stream().map(GoalRequirement::getInitializer).toList()));
            this.requirements = requirements;
        }
    }

    public static class OrCombined<T> extends Combinatorial<T> {
        public OrCombined(List<GoalRequirement<? super T>> goalRequirements) {
            super(goalRequirements);
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, T option) {
            return requirements.stream().anyMatch(r -> r.optionSatisfiedBy(context, option));
        }
    }

    public static class AndCombined<T> extends Combinatorial<T> {

        public AndCombined(List<GoalRequirement<? super T>> goalRequirements) {
            super(goalRequirements);
        }
        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, T option) {
            return requirements.stream().allMatch(r -> r.optionSatisfiedBy(context, option));
        }
    }

    public static class ExactlyCombined<T> extends Combinatorial<T> {
        private final int number;

        public ExactlyCombined(List<GoalRequirement<? super T>> goalRequirements, int number) {
            super(goalRequirements);
            this.number = number;
        }

        @Override
        public boolean optionSatisfiedBy(GoalRequirementContext context, T option) {
            return requirements.stream().filter(r -> r.optionSatisfiedBy(context, option)).count() == number;
        }
    }
}

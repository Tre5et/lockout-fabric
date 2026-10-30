package me.marin.lockout.lockout.goal.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface GoalGroup {
    Codec<GoalGroup> CODEC = LockoutRegistries.GOAL_GROUP_TYPE.byNameCodec().dispatch(GoalGroup::codec, c -> c);

    MapCodec<? extends GoalGroup> codec();

    boolean canAdd(ResourceKey<GoalBuilder<?,?>> goal, List<? extends Goal> existingGoals, Registry<GoalBuilder<?,?>> registry);

    default List<GoalBuilder<?,?>> goalsMatching(List<? extends Goal> goalBuilders, Registry<GoalBuilder<?,?>> registry) {
        return goalBuilders.stream()
                .map(g -> g.getBuildData().id())
                .map(registry::get)
                .filter(Optional::isPresent)
                .map(g -> g.get().value())
                .filter(g -> g.getGroups().stream().anyMatch(g1 -> this.equals(g1.value())))
                .collect(Collectors.toUnmodifiableList());
    }

    static void bootstrap(Registry<MapCodec<? extends GoalGroup>> registry) {
        LockoutRegistries.register(registry, "limited", Limited.MAP_CODEC);
    }

    static Limited withLimit(int limit) {
        return new GoalGroup.Limited(limit);
    }

    record Limited(int limit) implements GoalGroup {
        public static final MapCodec<Limited> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.fieldOf("limit").forGetter(Limited::limit)
        ).apply(i, Limited::new));

        @Override
        public MapCodec<? extends GoalGroup> codec() {
            return MAP_CODEC;
        }

        @Override
        public boolean canAdd(ResourceKey<GoalBuilder<?,?>> goal, List<? extends Goal> existingGoals, Registry<GoalBuilder<?,?>> registry) {
            return goalsMatching(existingGoals, registry).size() < limit;
        }
    }
}

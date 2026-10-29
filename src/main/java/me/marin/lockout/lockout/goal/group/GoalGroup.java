package me.marin.lockout.lockout.goal.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.registries.LockoutRegistries;
import net.minecraft.core.Registry;

import java.util.List;

public interface GoalGroup {
    Codec<GoalGroup> CODEC = LockoutRegistries.GOAL_GROUP_TYPE.byNameCodec().dispatch(GoalGroup::codec, c -> c);

    MapCodec<? extends GoalGroup> codec();

    boolean canAdd(GoalBuilder<?,?> goal, List<GoalBuilder<?,?>> existingGoals);

    default List<GoalBuilder<?,?>> goalsMatching(List<GoalBuilder<?,?>> goalBuilders) {
        return goalBuilders.stream().filter(g -> g.getGroups().stream().anyMatch(g1 -> this.equals(g1.value()))).toList();
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
        public boolean canAdd(GoalBuilder<?,?> goal, List<GoalBuilder<?,?>> existingGoals) {
            return goalsMatching(existingGoals).size() < limit;
        }
    }
}

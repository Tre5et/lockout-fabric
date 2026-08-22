package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.RideEntityGoal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.texture.GenericTextureRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RideEntityGoalBuilder extends GoalBuilder<Void> {
    protected final String name;
    protected final List<EntityType<?>> entityTypes;

    public RideEntityGoalBuilder(String id, GoalCategory category, String name, List<EntityType<?>> entityTypes) {
        super("RIDE_" + id, category);
        this.name = name;
        this.entityTypes = entityTypes;
    }

    @Override
    public GoalOptionGenerator<Void> optionGenerator() {
        return null;
    }

    @Override
    public Goal build(Void option) {
        return new RideEntityGoal(
                id,
                "Ride " + (List.of('a','e','i','o','u').contains(name.charAt(0)) ? "an " : "a ") + name,
                tooltipInfo,
                GenericTextureRenderer.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/ride_" + name.toLowerCase() + ".png")),
                new Pair<>(getId(), null),
                entityTypes::contains
        );
    }

    public static RideEntityGoalBuilder simple(String id, String name, GoalCategory category, EntityType<?>... entityTypes) {
        Arrays.stream(entityTypes)
                .map(BuiltInRegistries.ENTITY_TYPE::getKey)
                .map(Identifier::getPath)
                .toList();

        return new RideEntityGoalBuilder(
                id,
                category,
                name,
                Arrays.stream(entityTypes).toList()
        );
    }

    public static RideEntityGoalBuilder simple(GoalCategory category, EntityType<?>... entityTypes) {
        List<String> names = Arrays.stream(entityTypes)
                .map(BuiltInRegistries.ENTITY_TYPE::getKey)
                .map(Identifier::getPath)
                .toList();
        return simple(
                names.stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.joining("_OR_")),
                names.stream()
                    .map(n -> n.substring(0, 1).toUpperCase() + n.substring(1))
                    .collect(Collectors.joining(" or ")),
                category,
                entityTypes
        );
    }
}

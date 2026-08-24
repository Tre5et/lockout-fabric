package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.RideEntityGoal;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RideEntityGoalBuilder extends GoalBuilder<Void> {
    protected final List<EntityType<?>> entityTypes;

    public RideEntityGoalBuilder(String id, GoalCategory category, List<EntityType<?>> entityTypes) {
        super("RIDE_" + id, category);
        this.entityTypes = entityTypes;
    }

    @Override
    public NameExtractor defaultNameExtractor(Void option) {
        return NameExtractor.simple(() -> "Ride " + entityTypes.stream()
                .map(EntityType::toShortString)
                .map(s -> Arrays.stream(s.split("_"))
                        .map(p -> p.substring(0,1).toUpperCase() + p.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "))
                )
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/goals/ride/" + id.toLowerCase() + ".png"));
    }

    @Override
    public GoalOptionGenerator<Void> optionGenerator() {
        return null;
    }

    @Override
    public Goal build(Void option) {
        return new RideEntityGoal(
                id,
                getNameExtractor(option),
                getTextureExtractor(option),
                getTooltipInfo(option).orElse(null),
                getHints(null),
                new Pair<>(getStaticId(), null),
                entityTypes::contains
        );
    }

    public static RideEntityGoalBuilder simple(String id, GoalCategory category, EntityType<?>... entityTypes) {
        return new RideEntityGoalBuilder(
                id,
                category,
                Arrays.stream(entityTypes).toList()
        );
    }

    public static RideEntityGoalBuilder simple(GoalCategory category, EntityType<?>... entityTypes) {
        return simple(
                Arrays.stream(entityTypes)
                        .map(EntityType::toShortString)
                        .map(String::toUpperCase)
                        .collect(Collectors.joining("_OR_")),
                category,
                entityTypes
        );
    }
}

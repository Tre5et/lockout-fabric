package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreedAnimalGoalBuilder extends GoalBuilder<EntityUtil.BredEntity, Void> {
    private final List<EntityType<?>> acceptableEntities;

    public BreedAnimalGoalBuilder(String id, GoalCategory category, List<EntityType<?>> acceptableEntities) {
        super("BREED_" + id, category);
        this.acceptableEntities = acceptableEntities;
    }

    @Override
    public Component defaultName(Void option) {
        return Component.literal("Breed " + acceptableEntities.stream()
                .map(EntityUtil::getEntityName)
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return new CornerIconTextureExtractor(
                CycleTextureExtractor.texture(acceptableEntities.stream().map(EntityUtil::getEntityTexture).toList()),
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/full.png")),
        8);
    }

    @Override
    public ServerGoalProgress<EntityUtil.BredEntity, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(e -> acceptableEntities.contains(e.entity()));
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    public static BreedAnimalGoalBuilder any(String id, GoalCategory category, EntityType<?>... entities) {
        return new BreedAnimalGoalBuilder(id, category, Arrays.stream(entities).toList());
    }

    public static BreedAnimalGoalBuilder any(GoalCategory category, EntityType<?>... entities) {
        String id = Arrays.stream(entities)
                .map(EntityUtil::getEntityId)
                .collect(Collectors.joining("_OR_"));
        return new BreedAnimalGoalBuilder(id, category, Arrays.stream(entities).toList());
    }

    public static BreedAnimalGoalBuilder any(EntityType<?>... entities) {
        String id = Arrays.stream(entities)
                .map(EntityUtil::getEntityId)
                .collect(Collectors.joining("_OR_"));
        BreedAnimalGoalBuilder builder = new BreedAnimalGoalBuilder(id, GoalCategory.BREEDING, Arrays.stream(entities).toList());
        builder.group(GoalGroups.BREED);
        return builder;
    }
}

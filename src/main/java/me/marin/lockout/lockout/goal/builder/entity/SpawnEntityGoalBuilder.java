package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpawnEntityGoalBuilder extends GoalBuilder<EntityUtil.PlayerSpawnedEntity, Void> {
    private final List<EntityType<?>> acceptableEntities;

    public SpawnEntityGoalBuilder(String id, GoalCategory category, List<EntityType<?>> acceptableEntities) {
        super("SPAWN_" + id, category);
        this.acceptableEntities = acceptableEntities;
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        return Component.literal("Spawn " + acceptableEntities.stream()
                .map(EntityUtil::getEntityName)
                .collect(Collectors.joining(" or "))
        );
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return new CycleTextureExtractor(
            acceptableEntities.stream()
                    .map(e -> getSpawnEgg(e).map(s -> (TextureExtractor)ItemTextureExtractor.item(s)).orElse(GenericTextureExtractor.texture(EntityUtil.getEntityTexture(e))))
                    .toList()
        );
    }

    @Override
    public @NonNull ServerGoalProgress<EntityUtil.PlayerSpawnedEntity, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(e -> acceptableEntities.contains(e.entity()));
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(EntityUtil.PlayerSpawnedEntity update) {}

    public static SpawnEntityGoalBuilder any(EntityType<?>... entities) {
        String id = Arrays.stream(entities)
                .map(EntityUtil::getEntityId)
                .collect(Collectors.joining("_OR_"));
        return new SpawnEntityGoalBuilder(id, GoalCategory.SPAWNING, Arrays.stream(entities).toList());
    }

    private static Optional<Item> getSpawnEgg(EntityType<?> entity) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
        Identifier spawnEggId = Identifier.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
        return BuiltInRegistries.ITEM.get(spawnEggId).map(Holder.Reference::value);
    }
}

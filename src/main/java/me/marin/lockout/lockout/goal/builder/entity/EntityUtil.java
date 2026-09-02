package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class EntityUtil {
    public static String getEntityName(EntityType<?> entity) {
        return BuilderUtil.idToName(entity.toShortString());
    }

    public static Identifier getEntityTexture(EntityType<?> entity) {
        return Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/entity/" + entity.toShortString() + ".png");
    }

    public static TextureExtractor getEntityTextureExtractor(EntityType<?> entity) {
        return GenericTextureExtractor.texture(getEntityTexture(entity));
    }

    public static String getEntityId(EntityType<?> entity) {
        return entity.toShortString().toUpperCase();
    }

    public static Optional<Item> getSpawnEgg(EntityType<?> entity) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
        Identifier spawnEggId = Identifier.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
        return BuiltInRegistries.ITEM.get(spawnEggId).map(Holder.Reference::value);
    }

    public record BredEntity(
            EntityType<?> entity
    ) {}

    public record RodeEntity(
            EntityType<?> entity
    ) {}

    public record PlayerSpawnedEntity(
            EntityType<?> entity
    ) {}
}

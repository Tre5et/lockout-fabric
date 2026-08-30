package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EntityUtil {
    public static String getEntityName(EntityType<?> entity) {
        return Arrays.stream(entity.toShortString().split("_"))
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static Identifier getEntityTexture(EntityType<?> entity) {
        return Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/entity/" + entity.toShortString() + ".png");
    }

    public static String getEntityId(EntityType<?> entity) {
        return entity.toShortString().toUpperCase();
    }

    public record BredEntity(
            EntityType<?> entity
    ) {}

    public record RodeEntity(
            EntityType<?> entity
    ) {}
}

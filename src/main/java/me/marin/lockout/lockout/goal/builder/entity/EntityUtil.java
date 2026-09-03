package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;

import java.util.List;
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

    public static final List<EntityType<?>> BREEDABLE = List.of(
            EntityTypes.HORSE, EntityTypes.DONKEY,
            EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.GOAT, EntityTypes.SHEEP, EntityTypes.PIG, EntityTypes.CHICKEN,
            EntityTypes.WOLF, EntityTypes.CAT, EntityTypes.OCELOT,
            EntityTypes.AXOLOTL, EntityTypes.LLAMA, EntityTypes.TRADER_LLAMA,
            EntityTypes.RABBIT, EntityTypes.TURTLE, EntityTypes.PANDA,
            EntityTypes.FOX, EntityTypes.BEE, EntityTypes.STRIDER, EntityTypes.HOGLIN, EntityTypes.FROG,
            EntityTypes.CAMEL, EntityTypes.SNIFFER, EntityTypes.ARMADILLO, EntityTypes.NAUTILUS
    );

    public static final List<EntityType<?>> HOSTILE = List.of(
            EntityTypes.BLAZE, EntityTypes.BOGGED, EntityTypes.BREEZE, EntityTypes.CREAKING, EntityTypes.CREEPER,
            EntityTypes.ELDER_GUARDIAN, EntityTypes.ENDERMITE, EntityTypes.EVOKER, EntityTypes.GHAST,
            EntityTypes.GUARDIAN, EntityTypes.HOGLIN, EntityTypes.HUSK, EntityTypes.MAGMA_CUBE, EntityTypes.PARCHED,
            EntityTypes.PHANTOM, EntityTypes.PIGLIN_BRUTE, EntityTypes.PILLAGER, EntityTypes.RAVAGER,
            EntityTypes.SHULKER, EntityTypes.SILVERFISH, EntityTypes.SKELETON, EntityTypes.SLIME, EntityTypes.STRAY,
            EntityTypes.VEX, EntityTypes.VINDICATOR, EntityTypes.WARDEN, EntityTypes.WITCH,
            EntityTypes.WITHER_SKELETON, EntityTypes.ZOGLIN, EntityTypes.ZOMBIE, EntityTypes.ZOMBIE_VILLAGER
    );

    public static final List<EntityType<?>> ARTHROPODS = List.of(
            EntityTypes.BEE, EntityTypes.CAVE_SPIDER, EntityTypes.ENDERMITE,
            EntityTypes.SILVERFISH, EntityTypes.SPIDER
    );

    public static final List<EntityType<?>> UNDEAD = List.of(
            EntityTypes.CAMEL_HUSK, EntityTypes.DROWNED, EntityTypes.HUSK,
            EntityTypes.ZOMBIE_HORSE, EntityTypes.ZOMBIE_NAUTILUS, EntityTypes.ZOMBIE_VILLAGER,
            EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.BOGGED, EntityTypes.PARCHED, EntityTypes.SKELETON,
            EntityTypes.SKELETON_HORSE, EntityTypes.STRAY, EntityTypes.WITHER, EntityTypes.WITHER_SKELETON,
            EntityTypes.PHANTOM, EntityTypes.ZOGLIN, EntityTypes.ZOMBIE
    );

    public static final List<EntityType<?>> RAID = List.of(
            EntityTypes.PILLAGER, EntityTypes.VINDICATOR, EntityTypes.RAVAGER,
            EntityTypes.WITCH, EntityTypes.EVOKER, EntityTypes.VEX
    );

    public record BredEntity(
            EntityType<?> entity
    ) {}

    public record RodeEntity(
            EntityType<?> entity
    ) {}

    public record PlayerSpawnedEntity(
            EntityType<?> entity
    ) {}

    public record AngeredEntity(
            EntityType<?> entity
    ) {}
}

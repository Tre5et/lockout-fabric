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

    public static Identifier getBabyEntityTexture(EntityType<?> entity) {
        return getEntityTexture(entity).withSuffix("_baby");
    }

    public static TextureExtractor getEntityTextureExtractor(EntityType<?> entity) {
        return GenericTextureExtractor.texture(getEntityTexture(entity));
    }

    public static TextureExtractor getBabyEntityTextureExtractor(EntityType<?> entity) {
        return GenericTextureExtractor.texture(getBabyEntityTexture(entity));
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

    public static final List<EntityType<?>> LEASHABLE = List.of(
            EntityTypes.ALLAY, EntityTypes.ARMADILLO, EntityTypes.AXOLOTL, EntityTypes.BEE,
            EntityTypes.CAMEL, EntityTypes.CAT,
            EntityTypes.CHICKEN, EntityTypes.COPPER_GOLEM, EntityTypes.COW, EntityTypes.DOLPHIN,
            EntityTypes.DONKEY, EntityTypes.FOX, EntityTypes.FROG, EntityTypes.GLOW_SQUID,
            EntityTypes.GOAT, EntityTypes.HAPPY_GHAST, EntityTypes.HOGLIN, EntityTypes.HORSE,
            EntityTypes.IRON_GOLEM, EntityTypes.LLAMA, EntityTypes.MOOSHROOM, EntityTypes.MULE,
            EntityTypes.OCELOT, EntityTypes.PARROT, EntityTypes.PIG, EntityTypes.POLAR_BEAR,
            EntityTypes.RABBIT, EntityTypes.SHEEP, EntityTypes.SKELETON_HORSE, EntityTypes.SNIFFER,
            EntityTypes.SNOW_GOLEM, EntityTypes.SQUID, EntityTypes.STRIDER, EntityTypes.TRADER_LLAMA,
            EntityTypes.WOLF, EntityTypes.ZOGLIN, EntityTypes.NAUTILUS, EntityTypes.ZOMBIE_NAUTILUS,
            EntityTypes.CAMEL_HUSK, EntityTypes.ZOMBIE_HORSE
    );

    public static final List<EntityType<?>> AGE_LOCKABLE = List.of(
            EntityTypes.ARMADILLO, EntityTypes.AXOLOTL, EntityTypes.BEE, EntityTypes.CAMEL,
            EntityTypes.CHICKEN, EntityTypes.COW, EntityTypes.DOLPHIN, EntityTypes.DONKEY,
            EntityTypes.HORSE, EntityTypes.FOX, EntityTypes.HAPPY_GHAST, EntityTypes.GLOW_SQUID,
            EntityTypes.GOAT, EntityTypes.CAT, EntityTypes.LLAMA, EntityTypes.MOOSHROOM,
            EntityTypes.MULE, EntityTypes.NAUTILUS, EntityTypes.OCELOT, EntityTypes.PANDA,
            EntityTypes.PIG, EntityTypes.POLAR_BEAR, EntityTypes.WOLF, EntityTypes.RABBIT,
            EntityTypes.SHEEP, EntityTypes.SNIFFER, EntityTypes.SQUID, EntityTypes.STRIDER,
            EntityTypes.TRADER_LLAMA, EntityTypes.FROG, EntityTypes.HOGLIN, EntityTypes.SULFUR_CUBE
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

    public record TamedEntity(
            EntityType<?> entity
    ) {}

    public record AgeLockedEntity(
            EntityType<?> entity
    ) {}
}

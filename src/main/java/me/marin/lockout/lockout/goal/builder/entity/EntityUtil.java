package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityUtil {
    public static final Map<EntityType<?>, Item> ENTITY_ITEM_OVERRIDES = Map.ofEntries(
            Map.entry(EntityTypes.ARMOR_STAND, Items.ARMOR_STAND),
            Map.entry(EntityTypes.CUSHION, Items.CUSHION.red()),
            Map.entry(EntityTypes.ITEM_FRAME, Items.ITEM_FRAME),
            Map.entry(EntityTypes.GLOW_ITEM_FRAME, Items.ITEM_FRAME),
            Map.entry(EntityTypes.PAINTING, Items.PAINTING),
            Map.entry(EntityTypes.ARROW, Items.ARROW),
            Map.entry(EntityTypes.BLOCK_DISPLAY, Items.BARRIER),
            Map.entry(EntityTypes.EGG, Items.EGG),
            Map.entry(EntityTypes.ITEM, Items.STRUCTURE_VOID),
            Map.entry(EntityTypes.ITEM_DISPLAY, Items.STRUCTURE_VOID),
            Map.entry(EntityTypes.MINECART, Items.MINECART),
            Map.entry(EntityTypes.SNOWBALL, Items.SNOWBALL),
            Map.entry(EntityTypes.CHEST_MINECART, Items.CHEST_MINECART),
            Map.entry(EntityTypes.FURNACE_MINECART, Items.FURNACE_MINECART),
            Map.entry(EntityTypes.HOPPER_MINECART, Items.HOPPER_MINECART),
            Map.entry(EntityTypes.COMMAND_BLOCK_MINECART, Items.COMMAND_BLOCK_MINECART),
            Map.entry(EntityTypes.TNT_MINECART, Items.TNT_MINECART),
            Map.entry(EntityTypes.WIND_CHARGE, Items.WIND_CHARGE),
            Map.entry(EntityTypes.OAK_BOAT, Items.OAK_BOAT),
            Map.entry(EntityTypes.OAK_CHEST_BOAT, Items.OAK_CHEST_BOAT),
            Map.entry(EntityTypes.SPRUCE_BOAT, Items.SPRUCE_BOAT),
            Map.entry(EntityTypes.SPRUCE_CHEST_BOAT, Items.SPRUCE_CHEST_BOAT),
            Map.entry(EntityTypes.BIRCH_BOAT, Items.BIRCH_BOAT),
            Map.entry(EntityTypes.BIRCH_CHEST_BOAT, Items.BIRCH_CHEST_BOAT),
            Map.entry(EntityTypes.JUNGLE_BOAT, Items.JUNGLE_BOAT),
            Map.entry(EntityTypes.JUNGLE_CHEST_BOAT, Items.JUNGLE_CHEST_BOAT),
            Map.entry(EntityTypes.ACACIA_BOAT, Items.ACACIA_BOAT),
            Map.entry(EntityTypes.ACACIA_CHEST_BOAT, Items.ACACIA_CHEST_BOAT),
            Map.entry(EntityTypes.DARK_OAK_BOAT, Items.DARK_OAK_BOAT),
            Map.entry(EntityTypes.DARK_OAK_CHEST_BOAT, Items.DARK_OAK_CHEST_BOAT),
            Map.entry(EntityTypes.MANGROVE_BOAT, Items.MANGROVE_BOAT),
            Map.entry(EntityTypes.MANGROVE_CHEST_BOAT, Items.MANGROVE_CHEST_BOAT),
            Map.entry(EntityTypes.CHERRY_BOAT, Items.CHERRY_BOAT),
            Map.entry(EntityTypes.CHERRY_CHEST_BOAT, Items.CHERRY_CHEST_BOAT),
            Map.entry(EntityTypes.PALE_OAK_BOAT, Items.PALE_OAK_BOAT),
            Map.entry(EntityTypes.PALE_OAK_CHEST_BOAT, Items.PALE_OAK_CHEST_BOAT),
            Map.entry(EntityTypes.POPLAR_BOAT, Items.POPLAR_BOAT),
            Map.entry(EntityTypes.POPLAR_CHEST_BOAT, Items.POPLAR_CHEST_BOAT),
            Map.entry(EntityTypes.BAMBOO_RAFT, Items.BAMBOO_RAFT),
            Map.entry(EntityTypes.BAMBOO_CHEST_RAFT, Items.BAMBOO_CHEST_RAFT)
    );

    public static String getEntityName(EntityType<?> entity) {
        return BuilderUtil.idToName(entity.toShortString());
    }

    public static Identifier getEntityTexture(EntityType<?> entity) {
        return Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/entity/" + entity.toShortString() + ".png");
    }

    public static Identifier getBabyEntityTexture(EntityType<?> entity) {
        return Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/entity/" + entity.toShortString() + "_baby.png");
    }

    public static TextureExtractor getEntityTextureExtractor(EntityType<?> entity) {
        if(ENTITY_ITEM_OVERRIDES.containsKey(entity)) return ItemTextureExtractor.item(ENTITY_ITEM_OVERRIDES.get(entity));
        Identifier custom = getEntityTexture(entity);
        try {
            Minecraft.getInstance().getResourceManager().getResourceOrThrow(custom);
        } catch (IOException e) {
            Optional<Item> egg = getSpawnEgg(entity);
            if(egg.isPresent()) return ItemTextureExtractor.item(egg.get());
        }
        return GenericTextureExtractor.texture(custom);
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

    public record UsedItemOnEntity(
            ItemStack item,
            EntityType<?> entity
    ) {}
}

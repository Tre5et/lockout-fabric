package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.mixin.server.PlayerInventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemUtil {
    private static final Random RANDOM = new Random();

    public static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }

    public static String getItemId(Item item) {
        return BuilderUtil.identifierToId(BuiltInRegistries.ITEM.getKey(item));
    }

    public static TextureExtractor getItemTextureExtractor(Item item) {
        return new ItemTextureExtractor(item.getDefaultInstance());
    }

    @SuppressWarnings("unchecked")
    public static BannerPatternLayers getRandomBannerPattern(DyeColor overlayColor) {
        var bannerPatternRegistry = Minecraft.getInstance().level.registryAccess().lookup(Registries.BANNER_PATTERN);
        if(bannerPatternRegistry.isEmpty()) return BannerPatternLayers.EMPTY;
        Registry<?> patterns = bannerPatternRegistry.get();

        List<BannerPatternLayers.Layer> layers = new ArrayList<>();
        do {
            var selected = patterns.getRandom(RandomSource.create());
            if(selected.isEmpty() || !(selected.get().value() instanceof BannerPattern)) continue;
            layers.add(new BannerPatternLayers.Layer((Holder.Reference<BannerPattern>)selected.get(), overlayColor));
        } while(RANDOM.nextBoolean());

        return new BannerPatternLayers(layers);
    }

    public static List<Item> getAllItemsWithComponents(List<Item> items, List<DataComponentType<?>> componentTypes) {
        return items.stream()
                .filter(i -> {
                    ItemStack item = i.getDefaultInstance();
                    return componentTypes.stream().allMatch(item::has);
                }).toList();
    }

    public static <T> ItemStack applyComponent(ItemStack stack, DataComponentType<T> type, T value) {
        stack.set(type, value);
        return stack;
    }

    public static List<ItemStack> collectStacks(Inventory inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        for(ItemStack item : ((PlayerInventoryAccessor) inventory).getPlayerInventory()) {
            if (!item.isEmpty()) stacks.add(item);
        }
        var offHandItem = ((PlayerInventoryAccessor) inventory).getEquipment().get(EquipmentSlot.OFFHAND);
        if(!offHandItem.isEmpty()) stacks.add(offHandItem);
        return stacks;
    }

    public static List<Pair<Item, Integer>> collectCounts(Inventory inventory) {
        List<ItemStack> stacks = collectStacks(inventory);
        Map<Item, Integer> counts = new HashMap<>();
        for(ItemStack stack : stacks) {
            if(!counts.containsKey(stack.getItem())) {
                counts.put(stack.getItem(), stack.getCount());
            } else {
                counts.put(stack.getItem(), counts.get(stack.getItem()) + stack.getCount());
            }
        }
        return counts.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList();
    }

    public static final List<Item> POTTERY_SHERDS = List.of(
            Items.ANGLER_POTTERY_SHERD,
            Items.ARCHER_POTTERY_SHERD,
            Items.ARMS_UP_POTTERY_SHERD,
            Items.BLADE_POTTERY_SHERD,
            Items.BREWER_POTTERY_SHERD,
            Items.BURN_POTTERY_SHERD,
            Items.DANGER_POTTERY_SHERD,
            Items.EXPLORER_POTTERY_SHERD,
            Items.FLOW_POTTERY_SHERD,
            Items.FRIEND_POTTERY_SHERD,
            Items.GUSTER_POTTERY_SHERD,
            Items.HEART_POTTERY_SHERD,
            Items.HEARTBREAK_POTTERY_SHERD,
            Items.HOWL_POTTERY_SHERD,
            Items.MINER_POTTERY_SHERD,
            Items.MOURNER_POTTERY_SHERD,
            Items.PLENTY_POTTERY_SHERD,
            Items.PRIZE_POTTERY_SHERD,
            Items.SCRAPE_POTTERY_SHERD,
            Items.SHEAF_POTTERY_SHERD,
            Items.SHELTER_POTTERY_SHERD,
            Items.SKULL_POTTERY_SHERD,
            Items.SNORT_POTTERY_SHERD
    );

    public static final List<Item> HANGING_SIGN = List.of(
            Items.OAK_HANGING_SIGN,
            Items.SPRUCE_HANGING_SIGN,
            Items.BIRCH_HANGING_SIGN,
            Items.JUNGLE_HANGING_SIGN,
            Items.ACACIA_HANGING_SIGN,
            Items.CHERRY_HANGING_SIGN,
            Items.DARK_OAK_HANGING_SIGN,
            Items.PALE_OAK_HANGING_SIGN,
            Items.MANGROVE_HANGING_SIGN,
            Items.BAMBOO_HANGING_SIGN,
            Items.CRIMSON_HANGING_SIGN,
            Items.WARPED_HANGING_SIGN
    );

    public static final List<Item> ARMOR_TRIM = List.of(
            Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    public static final List<Item> HORSE_ARMOR = List.of(
            Items.COPPER_HORSE_ARMOR,
            Items.IRON_HORSE_ARMOR,
            Items.GOLDEN_HORSE_ARMOR,
            Items.DIAMOND_HORSE_ARMOR,
            Items.NETHERITE_HORSE_ARMOR,
            Items.LEATHER_HORSE_ARMOR
    );

    public static final List<Item> BANNER_PATTERN = List.of(
            Items.FLOWER_BANNER_PATTERN,
            Items.CREEPER_BANNER_PATTERN,
            Items.SKULL_BANNER_PATTERN,
            Items.MOJANG_BANNER_PATTERN,
            Items.GLOBE_BANNER_PATTERN,
            Items.PIGLIN_BANNER_PATTERN,
            Items.FLOW_BANNER_PATTERN,
            Items.GUSTER_BANNER_PATTERN,
            Items.FIELD_MASONED_BANNER_PATTERN,
            Items.BORDURE_INDENTED_BANNER_PATTERN
    );

    public static final List<Item> SAPLING = List.of(
            Items.SPRUCE_SAPLING,
            Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING,
            Items.ACACIA_SAPLING,
            Items.CHERRY_SAPLING,
            Items.DARK_OAK_SAPLING,
            Items.PALE_OAK_SAPLING,
            Items.MANGROVE_PROPAGULE
    );

    public static final List<Item> SEED = List.of(
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.WHEAT_SEEDS,
            Items.NETHER_WART
    );

    public static final List<Item> BUCKET = List.of(
            Items.BUCKET,
            Items.WATER_BUCKET,
            Items.LAVA_BUCKET,
            Items.POWDER_SNOW_BUCKET,
            Items.MILK_BUCKET,
            Items.PUFFERFISH_BUCKET,
            Items.SALMON_BUCKET,
            Items.COD_BUCKET,
            Items.TROPICAL_FISH_BUCKET,
            Items.AXOLOTL_BUCKET,
            Items.SULFUR_CUBE_BUCKET,
            Items.TADPOLE_BUCKET
    );

    public static final List<Item> FLOWER = List.of(
            Items.DANDELION,
            Items.GOLDEN_DANDELION,
            Items.OPEN_EYEBLOSSOM,
            Items.CLOSED_EYEBLOSSOM,
            Items.POPPY,
            Items.BLUE_ORCHID,
            Items.ALLIUM,
            Items.AZURE_BLUET,
            Items.RED_TULIP,
            Items.ORANGE_TULIP,
            Items.WHITE_TULIP,
            Items.PINK_TULIP,
            Items.OXEYE_DAISY,
            Items.CORNFLOWER,
            Items.LILY_OF_THE_VALLEY,
            Items.WITHER_ROSE,
            Items.TORCHFLOWER,
            Items.PITCHER_PLANT,
            Items.SPORE_BLOSSOM,
            Items.WILDFLOWERS,
            Items.PINK_PETALS,
            Items.FLOWERING_AZALEA_LEAVES,
            Items.FLOWERING_AZALEA
    );

    public static final List<Item> WORKSTATION = List.of(
            Items.LOOM,
            Items.COMPOSTER,
            Items.BARREL,
            Items.SMOKER,
            Items.BLAST_FURNACE,
            Items.CARTOGRAPHY_TABLE,
            Items.FLETCHING_TABLE,
            Items.GRINDSTONE,
            Items.SMITHING_TABLE,
            Items.STONECUTTER,
            Items.CAULDRON,
            Items.BREWING_STAND
    );

    public record BrewedItem(
            ItemStack itemStack
    ) {}

    public record CompostedItem(
            ItemStack itemStack
    ) {}

    public record CraftedItem(
            ItemStack itemStack
    ) {}

    public record ConsumedItem(
            ItemStack itemStack
    ) {}

    public record BrokenItem(
            Item item
    ) {}

    public record DataComponentCondition<T>(
            DataComponentType<T> component,
            Predicate<T> valueChecker,
            Consumer<ItemStack> valueApplier,
            Supplier<String> id,
            Supplier<String> name
    ) {
        public boolean test(ItemStack stack) {
            return stack.has(component) && valueChecker.test(stack.get(component));
        }

        public ItemStack apply(ItemStack stack) {
            valueApplier.accept(stack);
            return stack;
        }
    }
}

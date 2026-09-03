package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

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

    public static List<Item> POTTERY_SHERDS = List.of(
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

    public static List<Item> HANGING_SIGN = List.of(
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

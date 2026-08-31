package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
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
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.*;

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

    public static List<Item> getAllItemsWithComponents(List<DataComponentType<?>> componentTypes) {
        return BuiltInRegistries.ITEM.stream()
                .filter(i -> {
                    ItemStack item = i.getDefaultInstance();
                    return componentTypes.stream().allMatch(item::has);
                }).toList();
    }

    public static <T> ItemStack applyComponent(ItemStack stack, DataComponentType<T> type, T value) {
        stack.set(type, value);
        return stack;
    }

    public record BrewedItem(
            Item item
    ) {}

    public record CompostedItem(
            Item item
    ) {}

    public record CraftedItem(
            Item item
    ) {}

    public record ConsumedItem(
            ItemStack itemStack
    ) {}
}

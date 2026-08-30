package me.marin.lockout.lockout.goal.builder.item;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class ItemUtil {
    public static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }

    public static String getItemId(Item item) {
        return BuilderUtil.identifierToId(BuiltInRegistries.ITEM.getKey(item));
    }

    public record BrewedItem(
            Item item
    ) {}

    public record CompostedItem(
            Item item
    ) {}
}

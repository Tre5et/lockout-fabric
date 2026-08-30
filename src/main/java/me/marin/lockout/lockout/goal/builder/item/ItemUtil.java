package me.marin.lockout.lockout.goal.builder.item;

import net.minecraft.world.item.Item;

import java.util.Optional;

public class ItemUtil {
    public static String getItemName(Item item) {
        return item.getName(item.getDefaultInstance())
                .getContents().visit(Optional::of)
                .orElse(String.valueOf(Item.getId(item)));
    }
}

package me.marin.lockout.lockout.goal.builder.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InventoryUtil {
    public record UpdatedInventory<T>(
            T inventoryHolder,
            List<ItemStack> items,
            int maxSlotSize
    ) {}
}

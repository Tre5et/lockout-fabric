package me.marin.lockout.lockout.goal.builder.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
    public record MinedBlock(
            BlockState block
    ) {}

    public record UsedItemOnBlock(
            ItemStack item,
            BlockState block
    ) {}
}

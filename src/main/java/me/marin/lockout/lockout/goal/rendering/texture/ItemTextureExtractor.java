package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

public class ItemTextureExtractor implements TextureExtractor {
    private final ItemStack item;

    private ItemTextureExtractor(ItemStack item) {
        this.item = item;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int tick) {
        extractor.item(item, x, y);
        extractor.itemDecorations(font, item, x, y);
    }

    public static ItemTextureExtractor item(Item item) {
        return new ItemTextureExtractor(item.getDefaultInstance());
    }

    public static CycleTextureExtractor cycleItems(List<Item> items) {
        return CycleTextureExtractor.item(items);
    }

    public static ItemTextureExtractor stack(Item item, int count) {
        ItemStack stack = item.getDefaultInstance();
        stack.setCount(Math.clamp(count, 1, item.getDefaultMaxStackSize()));
        return new ItemTextureExtractor(stack);
    }

    public static CycleTextureExtractor cycleStacks(List<Pair<Item, Integer>> items) {
        return CycleTextureExtractor.itemStack(items);
    }
}

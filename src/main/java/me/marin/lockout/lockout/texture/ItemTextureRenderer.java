package me.marin.lockout.lockout.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

public class ItemTextureRenderer implements TextureRenderer {
    private final ItemStack item;

    private ItemTextureRenderer(ItemStack item) {
        this.item = item;
    }

    @Override
    public void renderTexture(GuiGraphicsExtractor extractor, Font font, int x, int y, int tick) {
        extractor.item(item, x, y);
        extractor.itemDecorations(font, item, x, y);
    }

    public static ItemTextureRenderer item(Item item) {
        return new ItemTextureRenderer(item.getDefaultInstance());
    }

    public static CycleTextureRenderer cycleItems(List<Item> items) {
        return CycleTextureRenderer.item(items);
    }

    public static ItemTextureRenderer stack(Item item, int count) {
        ItemStack stack = item.getDefaultInstance();
        stack.setCount(Math.clamp(count, 1, item.getDefaultMaxStackSize()));
        return new ItemTextureRenderer(stack);
    }

    public static CycleTextureRenderer cycleStacks(List<Pair<Item, Integer>> items) {
        return CycleTextureRenderer.itemStack(items);
    }
}

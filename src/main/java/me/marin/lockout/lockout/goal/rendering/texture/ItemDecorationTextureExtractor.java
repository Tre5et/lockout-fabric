package me.marin.lockout.lockout.goal.rendering.texture;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemDecorationTextureExtractor implements TextureExtractor {
    private final ItemStack item;

    public ItemDecorationTextureExtractor(ItemStack item) {
        this.item = item;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.itemDecorations(font, item, x, y);
        });
    }
}

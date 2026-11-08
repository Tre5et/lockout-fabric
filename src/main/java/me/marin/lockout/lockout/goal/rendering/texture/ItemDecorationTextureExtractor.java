package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.util.DynamicItemStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemDecorationTextureExtractor implements TextureExtractor {
    public static final MapCodec<ItemDecorationTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DynamicItemStack.CODEC.fieldOf("item").forGetter(ItemDecorationTextureExtractor::getItem)
    ).apply(instance, ItemDecorationTextureExtractor::new));

    @Getter
    private final DynamicItemStack item;

    public ItemDecorationTextureExtractor(ItemStack stack) {
        this(new DynamicItemStack(stack));
    }

    public ItemDecorationTextureExtractor(DynamicItemStack item) {
        this.item = item;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.itemDecorations(font, item.get(), x, y);
        });
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }
}

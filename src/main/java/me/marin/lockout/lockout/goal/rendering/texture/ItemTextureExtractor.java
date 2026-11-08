package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.util.DynamicItemStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

public class ItemTextureExtractor implements TextureExtractor {
    public static final MapCodec<ItemTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DynamicItemStack.CODEC.fieldOf("item").forGetter(ItemTextureExtractor::getItem)
    ).apply(instance, ItemTextureExtractor::new));

    @Getter
    private final DynamicItemStack item;

    public ItemTextureExtractor(ItemStack item) {
        this(new DynamicItemStack(item));
    }

    public ItemTextureExtractor(DynamicItemStack item) {
        this.item = item;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.fakeItem(item.get(), x, y);
            extractor.itemDecorations(font, item.get(), x, y);
        });
    }

    @Override
    public MapCodec<? extends TextureExtractor> codec() {
        return MAP_CODEC;
    }

    public static ItemTextureExtractor item(Item item) {
        return new ItemTextureExtractor(item.getDefaultInstance());
    }

    public static CycleTextureExtractor cycleItems(List<Item> items) {
        return CycleTextureExtractor.item(items);
    }

    public static ItemTextureExtractor stack(Item item, int count) {
        return new ItemTextureExtractor(new DynamicItemStack(() -> {
            ItemStack stack = item.getDefaultInstance();
            stack.setCount(count);
            return stack;
        }));
    }

    public static CycleTextureExtractor cycleStacks(List<Pair<Item, Integer>> items) {
        return CycleTextureExtractor.itemStack(items);
    }
}

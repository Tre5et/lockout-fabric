package me.marin.lockout.lockout.goal.rendering.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

public class ItemTextureExtractor implements TextureExtractor {
    public static final MapCodec<ItemTextureExtractor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Item.CODEC.fieldOf("item").forGetter(e -> e.item.getItem().builtInRegistryHolder()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(e -> e.item.count())
    ).apply(instance, (i,c) -> ItemTextureExtractor.stack(i.value(), c)));

    private final ItemStack item;

    public ItemTextureExtractor(ItemStack item) {
        this.item = item;
    }

    @Override
    public void extract(GuiGraphicsExtractor extractor, Font font, int x, int y, int width, int height, long tick) {
        float scale = Math.min(width/16f, height/16f);
        withScale(extractor, x, y, scale, () -> {
            extractor.fakeItem(item, x, y);
            extractor.itemDecorations(font, item, x, y);
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
        ItemStack stack = item.getDefaultInstance();
        stack.setCount(count);
        return new ItemTextureExtractor(stack);
    }

    public static CycleTextureExtractor cycleStacks(List<Pair<Item, Integer>> items) {
        return CycleTextureExtractor.itemStack(items);
    }
}

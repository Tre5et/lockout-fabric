package me.marin.lockout.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class DynamicItemStack {
    public static final Codec<DynamicItemStack> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(i -> i.group(
                    Item.CODEC.fieldOf("id").forGetter(s -> s.get().typeHolder()),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(s -> s.get().count()),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(s -> s.get().getComponentsPatch())
            ).apply(i, DynamicItemStack::new)),
            Item.CODEC,
            DynamicItemStack::new
    );

    @Getter
    private final Supplier<ItemStack> itemSupplier;
    private ItemStack cachedStack = null;

    public DynamicItemStack(ItemStack stack) {
        this(() -> stack);
    }

    public DynamicItemStack(Holder<Item> item, int count, DataComponentPatch components) {
        this(() -> new ItemStack(item, count, components));
    }

    public DynamicItemStack(Holder<Item> item) {
        this(() -> item.value().getDefaultInstance());
    }

    public DynamicItemStack(Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = itemSupplier;
    }

    public ItemStack get() {
        if(cachedStack == null) cachedStack = itemSupplier.get();
        return cachedStack;
    }
}

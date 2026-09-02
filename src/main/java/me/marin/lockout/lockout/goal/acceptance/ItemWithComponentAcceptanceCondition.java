package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemWithComponentAcceptanceCondition implements AcceptanceCondition<ItemStack> {
    private final List<ItemUtil.DataComponentCondition<?>> components;
    private final Supplier<List<ItemStack>> exampleItems;

    public ItemWithComponentAcceptanceCondition(List<ItemUtil.DataComponentCondition<?>> components, Supplier<List<ItemStack>> exampleItems) {
        this.components = components;
        this.exampleItems = exampleItems;
    }

    @Override
    public boolean test(ItemStack value) {
        return components.stream().allMatch(c -> c.test(value));
    }

    @Override
    public String getId() {
        return "COMPONENT_" + components.stream()
                .map(c -> c.id().get())
                .collect(Collectors.joining("_AND_"));
    }

    @Override
    public String getName() {
        return components.stream()
                .map(c -> c.name().get())
                .collect(Collectors.joining(" and ")) + " Item";
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return exampleItems.get().stream()
                .map(i -> {
                    ItemStack item = i.copy();
                    components.forEach(c -> c.apply(item));
                    return new ItemTextureExtractor(item);
                }).collect(Collectors.toUnmodifiableList());
    }

    public static ItemWithComponentAcceptanceCondition hasComponents(List<Item> items, DataComponentType<?>... components) {
        return new ItemWithComponentAcceptanceCondition(
                Arrays.stream(components).map(c -> new ItemUtil.DataComponentCondition<>(
                        c,
                        _ -> true,
                        _ -> {},
                        () -> BuilderUtil.identifierToId(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(c)),
                        () -> BuilderUtil.identifierToName(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(c))
                )).collect(Collectors.toUnmodifiableList()),
                () -> ItemUtil.getAllItemsWithComponents(items, Arrays.asList(components)).stream().map(Item::getDefaultInstance).toList()
        );
    }

    public static ItemWithComponentAcceptanceCondition hasComponents(DataComponentType<?>... components) {
        return hasComponents(BuiltInRegistries.ITEM.stream().toList(), components);
    }

    public static <T> ItemWithComponentAcceptanceCondition single(DataComponentType<T> component, T value, String id, String name, Item... items) {
        return new ItemWithComponentAcceptanceCondition(
                List.of(new ItemUtil.DataComponentCondition<>(
                        component, v -> v.equals(value),
                        i -> i.set(component, value),
                        () -> id, () -> name
                )),
                () -> Arrays.stream(items).map(Item::getDefaultInstance).toList()
        );
    }
}

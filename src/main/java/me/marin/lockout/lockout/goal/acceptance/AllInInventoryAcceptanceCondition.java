package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.mixin.server.PlayerInventoryAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AllInInventoryAcceptanceCondition<T,E> implements AcceptanceCondition<Inventory> {
    private final List<AcceptanceCondition<E>> conditions;
    private final Function<ItemStack, T> keyExtractor;
    private final BiFunction<ItemStack, E, E> aggregator;

    public AllInInventoryAcceptanceCondition(List<AcceptanceCondition<E>> conditions, Function<ItemStack, T> keyExtractor, BiFunction<ItemStack, E, E> aggregator) {
        this.conditions = conditions;
        this.keyExtractor = keyExtractor;
        this.aggregator = aggregator;
    }

    @Override
    public boolean test(Inventory value) {
        Map<T,E> data = new HashMap<>();
        for(ItemStack item : ((PlayerInventoryAccessor) value).getPlayerInventory()) {
            if(!item.isEmpty()) {
                T key = keyExtractor.apply(item);
                data.put(key, aggregator.apply(item, data.get(key)));
            }
        }
        var offHandItem = ((PlayerInventoryAccessor) value).getEquipment().get(EquipmentSlot.OFFHAND);
        if(!offHandItem.isEmpty()) {
            T key = keyExtractor.apply(offHandItem);
            data.put(key, aggregator.apply(offHandItem, data.get(key)));
        }

        return conditions.stream().allMatch(c -> {
            for(E element : data.values()) {
                if(c.test(element)) return true;
            }
            return false;
        });
    }

    @Override
    public String getId() {
        return conditions.stream().map(AcceptanceCondition::getId).collect(Collectors.joining("_AND_"));
    }

    @Override
    public String getName() {
        return conditions.stream().map(AcceptanceCondition::getName).collect(Collectors.joining(" and "));
    }

    @Override
    public List<TextureExtractor> getExamples() {
        return conditions.stream().flatMap(c -> c.getExamples().stream()).toList();
    }

    @SafeVarargs
    public static AllInInventoryAcceptanceCondition<ItemStack, ItemStack> singleSlot(AcceptanceCondition<ItemStack>... stacks) {
        return new AllInInventoryAcceptanceCondition<>(
                Arrays.asList(stacks),
                s -> s,
                (s,_) -> s
        );
    }

    public static AllInInventoryAcceptanceCondition<Item, ItemStack> allItems(Item... items) {
        return new AllInInventoryAcceptanceCondition<>(
                Arrays.stream(items).map(InListAcceptanceCondition::item).collect(Collectors.toUnmodifiableList()),
                ItemStack::getItem,
                (s, _) -> s
        );
    }

    public static AllInInventoryAcceptanceCondition<Item, ItemStack> anyItems(Item... items) {
        return new AllInInventoryAcceptanceCondition<>(
                List.of(InListAcceptanceCondition.item(items)),
                ItemStack::getItem,
                (s,_) -> s
        );
    }

    @SafeVarargs
    public static AllInInventoryAcceptanceCondition<Item, Pair<Item, Integer>> allItemsWithCount(Pair<Item, Integer>... items) {
        return new AllInInventoryAcceptanceCondition<>(
                Arrays.stream(items).map(InListAcceptanceCondition::itemWithCount).collect(Collectors.toUnmodifiableList()),
                ItemStack::getItem,
                (s,c) -> c == null ? new Pair<>(s.getItem(), s.count()) : new Pair<>(s.getItem(), c.getB() + s.count())
        );
    }
}

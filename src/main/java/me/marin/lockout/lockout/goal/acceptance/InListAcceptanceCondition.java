package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InListAcceptanceCondition<T,E> implements AcceptanceCondition<T> {
    private final List<E> acceptableElements;
    private final Function<T,E> toListElement;
    private final Function<E,String> toName;
    private final Function<E,T> toExample;

    public InListAcceptanceCondition(List<E> acceptableElements, Function<T, E> toListElement, Function<E, String> toName, Function<E, T> toExample) {
        this.acceptableElements = acceptableElements;
        this.toListElement = toListElement;
        this.toName = toName;
        this.toExample = toExample;
    }

    @Override
    public boolean test(T value) {
        return acceptableElements.contains(toListElement.apply(value));
    }

    @Override
    public String getName() {
        return acceptableElements.stream()
                .map(toName)
                .collect(Collectors.joining(" or "));
    }

    @Override
    public List<T> getExamples() {
        return acceptableElements.stream()
                .map(toExample).toList();
    }

    public static InListAcceptanceCondition<ItemStack, Item> item(Item... items) {
        return new InListAcceptanceCondition<>(
                Arrays.asList(items),
                ItemStack::getItem,
                ItemUtil::getItemName,
                Item::getDefaultInstance
        );
    }
}

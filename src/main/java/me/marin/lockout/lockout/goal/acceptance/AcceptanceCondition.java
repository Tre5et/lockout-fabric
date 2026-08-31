package me.marin.lockout.lockout.goal.acceptance;

import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface AcceptanceCondition<T> {
    boolean test(T value);

    String getName();

    List<T> getExamples();

    default ModifiedAcceptanceCondition<T> modify(Predicate<T> acceptanceChecker, Function<T, Stream<T>> modifyAndExpandExamples) {
        return new ModifiedAcceptanceCondition<>(this, acceptanceChecker, modifyAndExpandExamples);
    }

    default ModifiedAcceptanceCondition<T> modify(Predicate<T> acceptanceChecker) {
        return new ModifiedAcceptanceCondition<>(this, acceptanceChecker, Stream::of);
    }

    static ModifiedAcceptanceCondition<ItemStack> anyItemWithDefaultComponents(DataComponentType<?>... components) {
        return new AnyAcceptanceCondition<>(
                () -> Arrays.stream(components)
                        .map(c -> BuilderUtil.identifierToId(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(c)))
                        .collect(Collectors.joining(" and ")) + " Items",
                () -> ItemUtil.getAllItemsWithComponents(Arrays.asList(components)).stream().map(Item::getDefaultInstance).toList()
        ).modify(s -> Arrays.stream(components).anyMatch(s::has));
    }
}

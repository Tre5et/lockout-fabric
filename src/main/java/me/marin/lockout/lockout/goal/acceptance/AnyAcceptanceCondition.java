package me.marin.lockout.lockout.goal.acceptance;

import java.util.List;
import java.util.function.Supplier;

public class AnyAcceptanceCondition<T> implements AcceptanceCondition<T> {
    private final Supplier<String> nameSupplier;
    private final Supplier<List<T>> instanceSupplier;

    public AnyAcceptanceCondition(Supplier<String> nameSupplier, Supplier<List<T>> instanceSupplier) {
        this.nameSupplier = nameSupplier;
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public boolean test(T value) {
        return true;
    }

    @Override
    public String getName() {
        return nameSupplier.get();
    }

    @Override
    public List<T> getExamples() {
        return instanceSupplier.get();
    }
}

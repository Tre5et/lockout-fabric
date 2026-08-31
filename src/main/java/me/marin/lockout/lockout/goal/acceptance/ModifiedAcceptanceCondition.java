package me.marin.lockout.lockout.goal.acceptance;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModifiedAcceptanceCondition<T> implements AcceptanceCondition<T>  {
    private final AcceptanceCondition<T> previous;
    private final Predicate<T> checkModification;
    private final Function<T, Stream<T>> modifyAndExpandExamples;

    public ModifiedAcceptanceCondition(AcceptanceCondition<T> previous, Predicate<T> checkModification, Function<T, Stream<T>> modifyAndExpandExamples) {
        this.previous = previous;
        this.checkModification = checkModification;
        this.modifyAndExpandExamples = modifyAndExpandExamples;
    }

    @Override
    public boolean test(T value) {
        return previous.test(value) && checkModification.test(value);
    }

    @Override
    public String getName() {
        return previous.getName();
    }

    @Override
    public List<T> getExamples() {
        return previous.getExamples().stream()
                .flatMap(modifyAndExpandExamples)
                .toList();
    }
}

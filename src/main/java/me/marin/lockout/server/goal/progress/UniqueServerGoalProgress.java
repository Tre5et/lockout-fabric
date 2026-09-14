package me.marin.lockout.server.goal.progress;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UniqueServerGoalProgress<T,E> extends TargetNumberServerGoalProgress<T> {
    private final Set<E> values = new HashSet<>();
    private final Function<T,E> toElement;

    public UniqueServerGoalProgress(int target, BiPredicate<T,ServerPlayer> updatePredicate, Function<T,E> toElement) {
        super(target, updatePredicate);
        this.toElement = toElement;
    }

    @Override
    public Integer update(Integer current, T update, ServerPlayer player) {
        E element = toElement.apply(update);
        if(!values.contains(element)) {
            values.add(element);
            return super.update(current, update, player);
        }
        return current;
    }
}

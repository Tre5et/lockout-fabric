package me.marin.lockout.lockout.goal.tooltip;

public interface TooltipInfoProvider<T> {
    TooltipInfo get(T option);
}

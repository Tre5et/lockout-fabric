package me.marin.lockout.lockout.goal.builder.statistic;

import net.minecraft.resources.Identifier;

public class StatisticUtil {
    public record StatisticChanged(
            Identifier statistic,
            int amount
    ) {}
}

package me.marin.lockout.lockout.goal.builder.statistic;

import net.minecraft.stats.Stat;

public class StatisticUtil {
    public record StatisticChanged(
            Stat<?> statistic,
            int amount
    ) {}
}

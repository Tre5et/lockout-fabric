package me.marin.lockout.lockout.goal.builder.statistic;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.item.ItemUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChangeStatisticGoalBuilder<T> extends GoalBuilder<StatisticUtil.StatisticChanged, T> {
    public ChangeStatisticGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, StatisticUtil.StatisticChanged, ?> progressSupplier) {
        super("STATISTIC", "", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(StatisticUtil.StatisticChanged update) {}

    public static ChangeStatisticGoalBuilder<Void> custom(Supplier<TextureExtractor> extractor, Identifier... statistics) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(extractor, Arrays.stream(statistics).map(Stats.CUSTOM::get).toArray(Stat[]::new))).map(StatisticUtil.StatisticChanged::statistic)
        );
    }

    public static ChangeStatisticGoalBuilder<Integer> countCustom(int min, int max, int step, Function<Integer,Integer> scaler, Function<Integer,Integer> reverseScaler, String creation, String progress, Supplier<TextureExtractor> extractor, Identifier... statistics) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.integer(creation, min, max, step),
                GoalProgressSupplier.total(progress, _ -> InListAcceptanceCondition.statistic(extractor, Arrays.stream(statistics).map(Stats.CUSTOM::get).toArray(Stat[]::new)).map(StatisticUtil.StatisticChanged::statistic), StatisticUtil.StatisticChanged::amount, n -> String.valueOf(reverseScaler.apply(n.intValue()))).mapCreation(scaler::apply)
        );
    }

    public static GoalBuilder<StatisticUtil.StatisticChanged, Void> item(StatType<Item> stat, Item item) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(() -> ItemTextureExtractor.item(item), stat.get(item))).map(StatisticUtil.StatisticChanged::statistic)
        ).customName(_ -> "Use " + ItemUtil.getItemName(item));
    }

    public static GoalBuilder<StatisticUtil.StatisticChanged, Void> itemWhileRiding(StatType<Item> stat, Item item, EntityType<?> entityType) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,Stat<?>>simple(_ -> InListAcceptanceCondition.statistic(() -> ItemTextureExtractor.item(item), stat.get(item))
                        .withPlayerRequirement(p -> p.isPassenger() && p.getVehicle() != null && p.getVehicle().is(entityType))
                ).map(StatisticUtil.StatisticChanged::statistic)
        ).customName(_ -> "Use " + ItemUtil.getItemName(item));
    }
}

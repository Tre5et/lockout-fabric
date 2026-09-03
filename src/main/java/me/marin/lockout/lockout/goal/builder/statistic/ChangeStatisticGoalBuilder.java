package me.marin.lockout.lockout.goal.builder.statistic;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public class ChangeStatisticGoalBuilder<T> extends GoalBuilder<StatisticUtil.StatisticChanged, T> {
    public ChangeStatisticGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, StatisticUtil.StatisticChanged, ?> progressSupplier) {
        super("Statistic", "", GoalCategory.MISC_ACTIONS, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(StatisticUtil.StatisticChanged update) {}

    public static ChangeStatisticGoalBuilder<Void> any(Supplier<TextureExtractor> extractor, Identifier... statistics) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.<Void,Identifier>simple(_ -> InListAcceptanceCondition.statistic(extractor, statistics)).map(StatisticUtil.StatisticChanged::statistic)
        );
    }

    public static ChangeStatisticGoalBuilder<Integer> count(int min, int max, int step, Function<Integer,Integer> scaler, Function<Integer,Integer> reverseScaler, String creation, String progress, Supplier<TextureExtractor> extractor, Identifier... statistics) {
        return new ChangeStatisticGoalBuilder<>(
                GoalOptionSupplier.integer(creation, min, max, step),
                GoalProgressSupplier.total(progress, _ -> InListAcceptanceCondition.statistic(extractor, statistics).map(StatisticUtil.StatisticChanged::statistic), StatisticUtil.StatisticChanged::amount, n -> String.valueOf(reverseScaler.apply(n.intValue()))).mapCreation(scaler::apply)
        );
    }
}

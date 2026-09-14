package me.marin.lockout.lockout.goal.builder.statistic;

import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public class HaveStatusEffectGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick, T> {
    public HaveStatusEffectGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, List<MobEffectInstance>, ?> progressSupplier) {
        super("EFFECT", "Get", GoalCategory.STATUS_EFFECTS, optionSupplier, GoalProgressSupplier.player(progressSupplier.map(p -> p.getActiveEffects().stream().toList())));
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    @SafeVarargs
    public static HaveStatusEffectGoalBuilder<Void> any(Holder<MobEffect>... effects) {
        return new HaveStatusEffectGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.any(_ -> List.of(InListAcceptanceCondition.statusEffect(effects)))
        );
    }
}

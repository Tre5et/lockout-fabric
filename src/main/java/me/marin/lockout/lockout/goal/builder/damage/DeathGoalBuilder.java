package me.marin.lockout.lockout.goal.builder.damage;

import me.marin.lockout.lockout.goal.acceptance.EqualsAcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.function.Supplier;

public class DeathGoalBuilder<T> extends GoalBuilder<DamageUtil.PlayerDied, T> {
    public DeathGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, DamageUtil.PlayerDied, ?> progressSupplier) {
        super("DEATH", "Die to", GoalCategory.DEATH_DAMAGE, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(DamageUtil.PlayerDied update) {}

    public static DeathGoalBuilder<Void> type(ResourceKey<DamageType> type, Supplier<TextureExtractor> textureExtractorSupplier) {
        return new DeathGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> EqualsAcceptanceCondition.damageType(type, textureExtractorSupplier).mapEquals((s,t) -> s.source().typeHolder().is(t)))
        );
    }

    public static DeathGoalBuilder<Void> entity(EntityType<?>... entities) {
        return new DeathGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities).map(d -> d.source().getEntity() != null ? d.source().getEntity().getType() : (d.source().getDirectEntity() != null ? d.source().getDirectEntity().getType() : null)))
        );
    }

    @SafeVarargs
    public static DeathGoalBuilder<Void> fallLocation(Pair<FallLocation, Item>... locations) {
        return new DeathGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.fallLocation(Arrays.stream(locations)
                        .map(l -> new Pair<FallLocation, Supplier<TextureExtractor>>(l.getA(), () -> ItemTextureExtractor.item(l.getB()))).toList()
                ).map(DamageUtil.PlayerDied::player))
        );
    }
}

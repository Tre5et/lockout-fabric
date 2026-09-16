package me.marin.lockout.lockout.goal.builder.damage;

import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import net.minecraft.resources.Identifier;

public class TakeDamageGoalBuilder<T> extends GoalBuilder<DamageUtil.TakenDamage, T> {
    public TakeDamageGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Float, ?> progressSupplier) {
        super("TAKE_DAMAGE", "Take", GoalCategory.DEATH_DAMAGE, optionSupplier, progressSupplier.map(DamageUtil.TakenDamage::damage));
    }

    @Override
    public void reifiedUpdater(DamageUtil.TakenDamage update) {}

    public static TakeDamageGoalBuilder<Integer> total(int min, int max, int step) {
        return new TakeDamageGoalBuilder<>(
                GoalOptionSupplier.integer("Amount of damage to take", min, max, step),
                GoalProgressSupplier.rawTotal("Damage taken", () -> "Damage",
                        () -> GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/half.png"))
                ).mapCreation(i -> (float)i)
        );
    }
}

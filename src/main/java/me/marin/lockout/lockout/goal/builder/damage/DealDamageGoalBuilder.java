package me.marin.lockout.lockout.goal.builder.damage;

import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class DealDamageGoalBuilder<T> extends GoalBuilder<DamageUtil.DealtDamage, T> {
    public DealDamageGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Float, ?> progressSupplier) {
        super("DEAL_DAMAGE", "Deal", GoalCategory.DEATH_DAMAGE, optionSupplier, progressSupplier.map(DamageUtil.DealtDamage::damage));
    }

    @Override
    public void reifiedUpdater(DamageUtil.DealtDamage update) {}

    public static DealDamageGoalBuilder<Integer> total(int min, int max, int step) {
        return new DealDamageGoalBuilder<>(
                GoalOptionSupplier.integer("Amount of damage to deal", min, max, step),
                GoalProgressSupplier.<Float>total("Damage dealt", f -> f, "Damage", () -> new CornerIconTextureExtractor(
                        GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/half.png")),
                        ItemTextureExtractor.item(Items.IRON_SWORD),
                        10)
                ).mapCreation(i -> (float)i)
        );
    }
}

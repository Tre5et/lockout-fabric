package me.marin.lockout.lockout.goal.builder.damage;

import lombok.NonNull;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetFloatClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.TargetFloatServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class DealDamageGoalBuilder extends GoalBuilder<DamageUtil.DealtDamage, Integer> {
    private final int min;
    private final int max;
    private final int step;

    public DealDamageGoalBuilder(GoalCategory category, int min, int max, int step) {
        super("DEAL_DAMAGE_" + min + "_" + max, category);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, step, (max-min+1)/step));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Damage to deal", min, max, step, (max-min+1)/step));
    }

    @Override
    public @NonNull Component defaultName(Integer option) {
        return Component.literal("Deal " + option + " damage");
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                new CornerIconTextureExtractor(
                        GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/half.png")),
                        ItemTextureExtractor.item(Items.IRON_SWORD),
                10),
                new ItemCountTextureExtractor(Component.literal(option.toString()))
        ), 0);
    }

    @Override
    public @NonNull ServerGoalProgress<DamageUtil.DealtDamage, ?> getServerGoalProgress(Integer option) {
        return new TargetFloatServerGoalProgress<>((float)option, DamageUtil.DealtDamage::damage);
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetFloatClientGoalProgress("Damage dealt", (float)option);
    }

    @Override
    public void reifiedUpdater(DamageUtil.DealtDamage update) {}

    public static DealDamageGoalBuilder of(int min, int max, int step) {
        return new DealDamageGoalBuilder(GoalCategory.DEATH_DAMAGE, min, max, step);
    }
}

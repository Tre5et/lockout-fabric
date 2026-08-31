package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetNumberClientGoalProgress;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class AdvancementCountingGoalBuilder extends GoalBuilder<AdvancementHolder, Integer> {
    private final int min;
    private final int max;

    public AdvancementCountingGoalBuilder(String id, GoalCategory category, int min, int max) {
        super(id, category);
        this.min = min;
        this.max = max;
    }

    @Override
    public @NonNull Component defaultName(Integer option) {
        return Component.literal("Obtain " + option + " unique Advancements");
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_obtained.png")),
                TextTextureExtractor.text(String.valueOf(option))
        ), 3);
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, 1, max - min + 1));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Number of Advancements:", min, max, 1, max - min + 1));
    }

    @Override
    public @NonNull ServerGoalProgress<AdvancementHolder, ?> getServerGoalProgress(Integer option) {
        return new UniqueServerGoalProgress<>(option, a -> !a.id().getPath().startsWith("recipes/") && !a.id().getPath().endsWith("/root"));
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetNumberClientGoalProgress("Advancements obtained", option);
    }

    @Override
    public void reifiedUpdater(AdvancementHolder update) {}
}

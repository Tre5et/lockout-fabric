package me.marin.lockout.lockout.goal.builder.miscellanious;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

public class ObtainAdvancementGoalBuilder<T> extends GoalBuilder<AdvancementHolder, T> {

    public ObtainAdvancementGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Identifier, ?> progressSupplier, GoalDifficultySupplier<T> difficultySupplier) {
        super("ADVANCEMENT", "Obtain", GoalCategory.ADVANCEMENTS, optionSupplier, progressSupplier.map(AdvancementHolder::id), difficultySupplier);
    }

    @Override
    public void reifiedUpdater(AdvancementHolder update) {}

    public static ObtainAdvancementGoalBuilder<Void> any(GoalDifficultySupplier<Void> difficultySupplier, Identifier... advancements) {
        return new ObtainAdvancementGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.advancement(advancements)),
                difficultySupplier
        );
    }

    public static ObtainAdvancementGoalBuilder<Void> any(GoalDifficultySupplier<Void> difficultySupplier, String... advancementIds) {
        return any(difficultySupplier, Arrays.stream(advancementIds).map(Identifier::withDefaultNamespace).toArray(Identifier[]::new));
    }

    public static ObtainAdvancementGoalBuilder<Integer> unique(GoalDifficultySupplier<Integer> difficultySupplier, int min, int max, int step) {
        return new ObtainAdvancementGoalBuilder<>(
                GoalOptionSupplier.integer("Advancements to obtain", min, max, step),
                GoalProgressSupplier.unique("Advancements obtained", _ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(Identifier value, ServerPlayer player) {
                        return !value.getPath().startsWith("recipes/") && !value.getPath().endsWith("/root");
                    }

                    @Override
                    public String getId() {
                        return "";
                    }

                    @Override
                    public String getName() {
                        return "Advancements";
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/advancements/challenge_frame_obtained.png")));
                    }
                }),
                difficultySupplier
        );
    }
}

package me.marin.lockout.lockout.goal.builder.advancement;

import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.List;

public class ObtainAdvancementGoalBuilder<T> extends GoalBuilder<AdvancementHolder, T> {

    public ObtainAdvancementGoalBuilder(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, Identifier, ?> progressSupplier) {
        super("ADVANCEMENT", "Obtain", GoalCategory.ADVANCEMENTS, optionSupplier, progressSupplier.map(AdvancementHolder::id));
    }

    @Override
    public void reifiedUpdater(AdvancementHolder update) {}

    public static ObtainAdvancementGoalBuilder<Void> any(Identifier... advancements) {
        return new ObtainAdvancementGoalBuilder<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.advancement(advancements))
        );
    }

    public static ObtainAdvancementGoalBuilder<Void> any(String... advancementIds) {
        return any(Arrays.stream(advancementIds).map(Identifier::withDefaultNamespace).toArray(Identifier[]::new));
    }

    public static ObtainAdvancementGoalBuilder<Integer> unique(int min, int max, int step) {
        return new ObtainAdvancementGoalBuilder<>(
                GoalOptionSupplier.integer("Advancements to obtain", min, max, step),
                GoalProgressSupplier.unique("Advancements obtained", _ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(Identifier value) {
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
                })
        );
    }
}

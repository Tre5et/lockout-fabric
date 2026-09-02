package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class PlayerStateGoalBuilder<T> extends GoalBuilder<ServerPlayer, T> {
    public PlayerStateGoalBuilder(GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ServerPlayer, ?> progressSupplier) {
        super("PLAYER", "", category, optionSupplier, progressSupplier);
    }

    @Override
    public void reifiedUpdater(ServerPlayer update) {}

    public static PlayerStateGoalBuilder<Void> emptyHungerBar() {
        return new PlayerStateGoalBuilder<>(GoalCategory.MISC_ACTIONS,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(ServerPlayer value) {
                        return value.getFoodData().getFoodLevel() == 0;
                    }

                    @Override
                    public String getId() {
                        return "HUNGER_EMPTY";
                    }

                    @Override
                    public String getName() {
                        return "Empty Hunger Bar";
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(new StackingTextureExtractor(List.of(
                                SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/food_empty_hunger")),
                                SpriteTextureExtractor.sprite(Identifier.withDefaultNamespace("hud/food_half_hunger"))
                        ), 0));
                    }
                })
        );
    }

    public static PlayerStateGoalBuilder<Void> heightAbove(int height, ResourceKey<Level> dimension) {
        return new PlayerStateGoalBuilder<>(GoalCategory.MISC_ACTIONS,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(ServerPlayer value) {
                        return value.getY() > height && value.level().dimension() == dimension;
                    }

                    @Override
                    public String getId() {
                        return "HEIGHT_" + height + "_" + BuilderUtil.identifierToId(dimension.identifier());
                    }

                    @Override
                    public String getName() {
                        return "Reach height " + height + " in " + BuilderUtil.identifierToName(dimension.identifier());
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(
                                new CornerIconTextureExtractor(
                                        GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/particle/geyser_plume_04.png")),
                                        GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/up_arrow.png")),
                                8)
                        );
                    }
                })

        );
    }
}

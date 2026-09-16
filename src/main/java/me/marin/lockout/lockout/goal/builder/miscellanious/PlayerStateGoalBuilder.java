package me.marin.lockout.lockout.goal.builder.miscellanious;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.InListAcceptanceCondition;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.option.GoalOptionSupplier;
import me.marin.lockout.lockout.goal.progress.GoalProgressSupplier;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PlayerStateGoalBuilder<T> extends GoalBuilder<BuilderUtil.Tick, T> {
    public PlayerStateGoalBuilder(GoalCategory category, GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, ServerPlayer, ?> progressSupplier) {
        super("PLAYER", "", category, optionSupplier, GoalProgressSupplier.player(progressSupplier));
    }

    @Override
    public void reifiedUpdater(BuilderUtil.Tick update) {}

    public static PlayerStateGoalBuilder<Void> emptyHungerBar() {
        return new PlayerStateGoalBuilder<>(GoalCategory.MISC_ACTIONS,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(ServerPlayer value, ServerPlayer player) {
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
                    public boolean test(ServerPlayer value, ServerPlayer player) {
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

    public static PlayerStateGoalBuilder<Void> sleepIn(Block... blocks) {
        return new PlayerStateGoalBuilder<>(
                GoalCategory.MISC_ACTIONS,
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<ServerPlayer>() {
                    private final InListAcceptanceCondition<BlockState, Block> condition = InListAcceptanceCondition.block(blocks);

                    @Override
                    public boolean test(ServerPlayer value, ServerPlayer player) {
                        if(!value.isSleeping() || value.getSleepingPos().isEmpty()) return false;
                        BlockState block = value.level().getBlockState(value.getSleepingPos().get());
                        return condition.test(block, player);
                    }

                    @Override
                    public String getId() {
                        return "SLEEP_" + condition.getId();
                    }

                    @Override
                    public String getName() {
                        return "Sleep in " + condition.getName();
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples();
                    }
                })
        );
    }
}

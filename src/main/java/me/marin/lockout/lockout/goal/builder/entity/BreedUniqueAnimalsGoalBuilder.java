package me.marin.lockout.lockout.goal.builder.entity;

import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetNumberClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.group.GoalGroups;
import me.marin.lockout.lockout.goal.option.GoalOptionGenerator;
import me.marin.lockout.lockout.goal.option.IntegerGoalOptionGenerator;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemCountTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public class BreedUniqueAnimalsGoalBuilder extends GoalBuilder<EntityUtil.BredEntity, Integer> {
    private final int min;
    private final int max;

    public BreedUniqueAnimalsGoalBuilder(String id, GoalCategory category, int min, int max) {
        super("BREED_ANIMALS_COUNT_" + id, category);
        this.min = min;
        this.max = max;
    }

    @Override
    public Optional<GoalOptionGenerator<Integer>> getOptionGenerator() {
        return Optional.of(new IntegerGoalOptionGenerator(min, max, 1, max-min+1));
    }

    @Override
    public Optional<ClientGoalOptionGenerator<Integer>> getClientOptionGenerator() {
        return Optional.of(new IntegerClientGoalOptionGenerator("Number of unique animals", min, max, 1, max-min+1));
    }

    @Override
    public Component defaultName(Integer option) {
        return Component.literal("Breed " + option + " Unique animals");
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Integer option) {
        return new StackingTextureExtractor(List.of(
                GenericTextureExtractor.texture(Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/full.png")),
                new ItemCountTextureExtractor(Component.literal(option.toString()))
        ), 0);
    }

    @Override
    public ServerGoalProgress<EntityUtil.BredEntity, ?> getServerGoalProgress(Integer option) {
        return new UniqueServerGoalProgress<>(option, _ -> true);
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Integer option) {
        return new TargetNumberClientGoalProgress("Animals bred", option);
    }

    @Override
    public void reifiedUpdater(EntityUtil.BredEntity update) {}

    public static BreedUniqueAnimalsGoalBuilder range(int min, int max) {
        BreedUniqueAnimalsGoalBuilder builder = new BreedUniqueAnimalsGoalBuilder(min + "_" + max, GoalCategory.BREEDING, min, max);
        builder.group(GoalGroups.BREED);
        return builder;
    }
}

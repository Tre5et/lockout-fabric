package me.marin.lockout.lockout.goal.builder;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.*;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RideEntityGoalBuilder extends GoalBuilder<EntityUtil.RodeEntity, Void> {
    protected final List<EntityType<?>> entityTypes;

    public RideEntityGoalBuilder(String id, GoalCategory category, List<EntityType<?>> entityTypes) {
        super("RIDE_" + id, category);
        this.entityTypes = entityTypes;
    }

    @Override
    public Component defaultName(Void option) {
        return Component.literal("Ride " + entityTypes.stream()
                .map(EntityUtil::getEntityName)
                .collect(Collectors.joining(" or ")
        ));
    }

    @Override
    public TextureExtractor defaultTextureExtractor(Void option) {
        return new CornerIconTextureExtractor(
                CycleTextureExtractor.texture(entityTypes.stream().map(EntityUtil::getEntityTexture).toList()),
                ItemTextureExtractor.item(Items.SADDLE),
        10);
    }

    @Override
    public ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(EntityUtil.RodeEntity update) {}

    @Override
    public ServerGoalProgress<EntityUtil.RodeEntity, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(e -> entityTypes.contains(e.entity()));
    }

    public static RideEntityGoalBuilder simple(String id, GoalCategory category, EntityType<?>... entityTypes) {
        return new RideEntityGoalBuilder(
                id,
                category,
                Arrays.stream(entityTypes).toList()
        );
    }

    public static RideEntityGoalBuilder simple(GoalCategory category, EntityType<?>... entityTypes) {
        return simple(
                Arrays.stream(entityTypes)
                        .map(EntityUtil::getEntityId)
                        .collect(Collectors.joining("_OR_")),
                category,
                entityTypes
        );
    }
}

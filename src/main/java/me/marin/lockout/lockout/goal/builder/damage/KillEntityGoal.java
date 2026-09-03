package me.marin.lockout.lockout.goal.builder.damage;

import com.google.gson.reflect.TypeToken;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KillEntityGoal<T> extends GoalBuilder<DamageUtil.KilledEntity, T> {
    public KillEntityGoal(GoalOptionSupplier<T> optionSupplier, GoalProgressSupplier<T, DamageUtil.KilledEntity, ?> progressSupplier) {
        super("KILL", "Kill", GoalCategory.KILLING, optionSupplier, progressSupplier);
    }

    @Override
    public TextureExtractor applyTextureExtractor(TextureExtractor textureExtractor, T option) {
        return new CornerIconTextureExtractor(
                textureExtractor,
                ItemTextureExtractor.item(Items.IRON_SWORD),
        10);
    }

    @Override
    public void reifiedUpdater(DamageUtil.KilledEntity update) {}

    public static KillEntityGoal<Void> any(EntityType<?>... entities) {
        return new KillEntityGoal<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> InListAcceptanceCondition.entity(entities).map(e -> e.entity().getType()))
        );
    }

    public static KillEntityGoal<Integer> unique(int min, int max, EntityType<?>... entities) {
        return new KillEntityGoal<>(
                GoalOptionSupplier.integer("Mobs to kill", min, max, 1),
                GoalProgressSupplier.unique("Mobs killed", _ -> InListAcceptanceCondition.entity(entities).map(e -> e.entity().getType()), e -> e.entity().getType())
        );
    }

    public static KillEntityGoal<Integer> total(int min, int max, int step, EntityType<?>... entities) {
        return new KillEntityGoal<>(
                GoalOptionSupplier.integer("Mobs to kill", min, max, step),
                GoalProgressSupplier.countTotal("Mobs killed", _ -> InListAcceptanceCondition.entity(entities).map(e -> e.entity().getType()))
        );
    }

    public static KillEntityGoal<DyeColor> coloredSheep() {
        return new KillEntityGoal<>(
                GoalOptionSupplier.list("Color", DyeColor.VALUES, new TypeToken<>() {}, "Color", DyeColor::toString),
                GoalProgressSupplier.simple(c -> new AcceptanceCondition<>() {
                    @Override
                    public boolean test(DamageUtil.KilledEntity value) {
                        return value.entity().getType() == EntityTypes.SHEEP && ((Sheep) value.entity()).getColor() == c;
                    }

                    @Override
                    public String getId() {
                        return "COLORED_SHEEP";
                    }

                    @Override
                    public String getName() {
                        return BuilderUtil.idToName(c.getName()) + " Sheep";
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return List.of(GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/entity/sheep_" + c.getName() + ".png")));
                    }
                })
        );
    }

    public static KillEntityGoal<Void> deathType(ResourceKey<DamageType> type, Supplier<TextureExtractor> icon, EntityType<?>... entities) {
        return new KillEntityGoal<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities);

                    @Override
                    public boolean test(DamageUtil.KilledEntity value) {
                        return condition.test(value.entity().getType()) && value.source().typeHolder().is(type);
                    }

                    @Override
                    public String getId() {
                        return "METHOD_" + BuilderUtil.identifierToId(type.identifier()) + "_" + condition.getId();
                    }

                    @Override
                    public String getName() {
                        return condition.getName() + " using " + BuilderUtil.identifierToName(type.identifier());
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples().stream().map(e -> new StackingTextureExtractor(
                                List.of(e, icon.get()), 6
                        )).collect(Collectors.toUnmodifiableList());
                    }
                })
        );
    }

    public static KillEntityGoal<Void> dimension(ResourceKey<Level> dimension, Supplier<TextureExtractor> icon, EntityType<?>... entities) {
        return new KillEntityGoal<>(
                GoalOptionSupplier.NONE,
                GoalProgressSupplier.simple(_ -> new AcceptanceCondition<>() {
                    private final InListAcceptanceCondition<EntityType<?>, EntityType<?>> condition = InListAcceptanceCondition.entity(entities);

                    @Override
                    public boolean test(DamageUtil.KilledEntity value) {
                        return condition.test(value.entity().getType()) && value.entity().level().dimension() == dimension;
                    }

                    @Override
                    public String getId() {
                        return "DIMENSION_" + BuilderUtil.identifierToId(dimension.identifier()) + "_" + condition.getId();
                    }

                    @Override
                    public String getName() {
                        return condition.getName() + " in " + BuilderUtil.identifierToName(dimension.identifier());
                    }

                    @Override
                    public List<TextureExtractor> getExamples() {
                        return condition.getExamples().stream().map(e -> new StackingTextureExtractor(
                                List.of(e, icon.get()), 6
                        )).collect(Collectors.toUnmodifiableList());
                    }
                })
        );
    }
}

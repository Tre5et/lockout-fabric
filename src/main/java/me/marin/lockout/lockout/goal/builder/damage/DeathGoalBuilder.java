package me.marin.lockout.lockout.goal.builder.damage;

import lombok.NonNull;
import me.marin.lockout.Constants;
import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.lockout.goal.builder.BuilderUtil;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.rendering.texture.CornerIconTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeathGoalBuilder extends GoalBuilder<DamageUtil.PlayerDied, Void> {
    private final Component name;
    private final Supplier<TextureExtractor> baseTextureExtractor;
    private final Predicate<DamageSource> damageSourcePredicate;

    public DeathGoalBuilder(String id, GoalCategory category, Component name, Supplier<TextureExtractor> baseTextureExtractor, Predicate<DamageSource> damageSourcePredicate) {
        super("DEATH_" + id, category);
        this.name = name;
        this.baseTextureExtractor = baseTextureExtractor;
        this.damageSourcePredicate = damageSourcePredicate;
    }

    @Override
    public @NonNull Component defaultName(Void option) {
        return name;
    }

    @Override
    public @NonNull TextureExtractor defaultTextureExtractor(Void option) {
        return new CornerIconTextureExtractor(
                baseTextureExtractor.get(),
                GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/death.png")),
        7);
    }

    @Override
    public @NonNull ServerGoalProgress<DamageUtil.PlayerDied, ?> getServerGoalProgress(Void option) {
        return new SimpleServerGoalProgress<>(d -> damageSourcePredicate.test(d.source()));
    }

    @Override
    public @NonNull ClientGoalProgress<?> getClientGoalProgress(Void option) {
        return new SimpleClientGoalProgress();
    }

    @Override
    public void reifiedUpdater(DamageUtil.PlayerDied update) {}

    @SafeVarargs
    public static DeathGoalBuilder type(String name, Supplier<TextureExtractor> baseTextureExtractor, ResourceKey<DamageType>... types) {
        Predicate<DamageSource> predicate = (s) -> Arrays.stream(types).anyMatch(s.typeHolder()::is);
        String id = Arrays.stream(types)
                .map(t -> BuilderUtil.identifierToId(t.identifier()))
                .collect(Collectors.joining("_OR_"));
        return new DeathGoalBuilder(id, GoalCategory.DEATH_DAMAGE, Component.literal(name), baseTextureExtractor, predicate);
    }

    public static DeathGoalBuilder entity(EntityType<?>... entities) {
        Predicate<DamageSource> predicate = s -> s.getEntity() != null && Arrays.asList(entities).contains(s.getEntity().getType()) || s.getDirectEntity() != null && Arrays.asList(entities).contains(s.getDirectEntity().getType());
        String id = "ENTITY_" + Arrays.stream(entities)
                .map(EntityUtil::getEntityId)
                .collect(Collectors.joining("_OR_"));
        Component name = Component.literal("Die to " + Arrays.stream(entities)
                .map(EntityUtil::getEntityName)
                .collect(Collectors.joining(" or "))
        );
        Supplier<TextureExtractor> extractor = () -> new CycleTextureExtractor(Arrays.stream(entities)
                .map(e -> GenericTextureExtractor.texture(EntityUtil.getEntityTexture(e)))
                .toList()
        );
        return new DeathGoalBuilder(id, GoalCategory.DEATH_DAMAGE, name, extractor, predicate);
    }
}

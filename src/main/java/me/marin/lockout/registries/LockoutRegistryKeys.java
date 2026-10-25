package me.marin.lockout.registries;

import com.mojang.serialization.MapCodec;
import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class LockoutRegistryKeys {
    public static final ResourceKey<Registry<MapCodec<? extends DifficultyCheckFunction>>> DIFFICULTY_CHECK_TYPE = createRegistryKey("difficulty_check_type");
    public static final ResourceKey<Registry<MapCodec<? extends DifficultyCheckFunction.Modifier>>> DIFFICULTY_CHECK_MODIFIER_TYPE = createRegistryKey("difficulty_check_modifier_type");
    public static final ResourceKey<Registry<MapCodec<? extends TextureExtractor>>> TEXTURE_EXTRACTOR_TYPE = createRegistryKey("texture_extractor_type");
    public static final ResourceKey<Registry<MapCodec<? extends GoalDifficultySupplier<?>>>> DIFFICULTY_SUPPLIER_TYPE = createRegistryKey("difficult_supplier_type");
    public static final ResourceKey<Registry<MapCodec<? extends GoalRequirement>>> GOAL_REQUIREMENT_TYPE = createRegistryKey("goal_requirement_type");
    public static final ResourceKey<Registry<GameDifficulty>> DIFFICULTY = createRegistryKey("difficulty");
    public static final ResourceKey<Registry<GoalRequirement>> GOAL_REQUIREMENT = createRegistryKey("goal_requirement");

    public static void initialize() {
        DynamicRegistries.registerSynced(DIFFICULTY, GameDifficulty.CODEC);
        DynamicRegistries.registerSynced(GOAL_REQUIREMENT, GoalRequirement.CODEC);
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(final String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, name));
    }
}

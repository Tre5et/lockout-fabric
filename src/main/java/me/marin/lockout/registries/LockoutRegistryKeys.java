package me.marin.lockout.registries;

import com.mojang.serialization.MapCodec;
import me.marin.lockout.Constants;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.config.GoalCategory;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.group.GoalGroup;
import me.marin.lockout.lockout.goal.hint.HintCombination;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.server.goal.hint.ServerHint;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class LockoutRegistryKeys {
    public static final ResourceKey<Registry<MapCodec<? extends DifficultyCheckFunction>>> DIFFICULTY_CHECK_TYPE = createRegistryKey("difficulty_check_type");
    public static final ResourceKey<Registry<MapCodec<? extends DifficultyCheckFunction.Modifier>>> DIFFICULTY_CHECK_MODIFIER_TYPE = createRegistryKey("difficulty_check_modifier_type");
    public static final ResourceKey<Registry<MapCodec<? extends TextureExtractor>>> TEXTURE_EXTRACTOR_TYPE = createRegistryKey("texture_extractor_type");
    public static final ResourceKey<Registry<MapCodec<? extends GoalDifficultySupplier<? super Number>>>> DIFFICULTY_SUPPLIER_TYPE = createRegistryKey("difficult_supplier_type");
    public static final ResourceKey<Registry<MapCodec<? extends GoalRequirement>>> GOAL_REQUIREMENT_TYPE = createRegistryKey("goal_requirement_type");
    public static final ResourceKey<Registry<GameDifficulty>> DIFFICULTY = createRegistryKey("difficulty");
    public static final ResourceKey<Registry<GoalRequirement>> GOAL_REQUIREMENT = createRegistryKey("goal_requirement");
    public static final ResourceKey<Registry<MapCodec<? extends ServerHint<?>>>> SERVER_HINT_TYPE = createRegistryKey("server_hint_type");
    public static final ResourceKey<Registry<MapCodec<? extends ClientHint<?>>>> CLIENT_HINT_TYPE = createRegistryKey("client_hint_type");
    public static final ResourceKey<Registry<HintCombination<?>>> GOAL_HINT = createRegistryKey("goal_hint");
    public static final ResourceKey<Registry<MapCodec<? extends GoalBuilder<?,?>>>> GOAL_BUILDER_TYPE = createRegistryKey("goal_builder_type");
    public static final ResourceKey<Registry<GoalBuilder<?,?>>> GOAL = createRegistryKey("goal");
    public static final ResourceKey<Registry<MapCodec<? extends GoalGroup>>> GOAL_GROUP_TYPE = createRegistryKey("goal_group_type");
    public static final ResourceKey<Registry<GoalGroup>> GOAL_GROUP = createRegistryKey("goal_group");
    public static final ResourceKey<Registry<GoalCategory>> GOAL_CATEGORY = createRegistryKey("goal_category");

    public static void initialize() {
        DynamicRegistries.registerSynced(DIFFICULTY, GameDifficulty.CODEC);
        DynamicRegistries.registerSynced(GOAL_REQUIREMENT, GoalRequirement.CODEC);
        DynamicRegistries.registerSynced(GOAL_HINT, HintCombination.CODEC);
        DynamicRegistries.registerSynced(GOAL, GoalBuilder.CODEC);
        DynamicRegistries.registerSynced(GOAL_GROUP, GoalGroup.CODEC);
        DynamicRegistries.registerSynced(GOAL_CATEGORY, GoalCategory.CODEC);
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(final String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, name));
    }
}

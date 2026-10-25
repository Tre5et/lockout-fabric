package me.marin.lockout.registries;

import com.mojang.serialization.MapCodec;
import me.marin.lockout.Constants;
import me.marin.lockout.client.goal.hint.ClientHint;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import me.marin.lockout.lockout.goal.difficulty.GoalDifficultySupplier;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.server.goal.hint.ServerHint;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class LockoutRegistries {
    public static final Registry<MapCodec<? extends DifficultyCheckFunction>> DIFFICULTY_CHECK_TYPE = register(LockoutRegistryKeys.DIFFICULTY_CHECK_TYPE);
    public static final Registry<MapCodec<? extends DifficultyCheckFunction.Modifier>> DIFFICULTY_CHECK_MODIFIER_TYPE = register(LockoutRegistryKeys.DIFFICULTY_CHECK_MODIFIER_TYPE);
    public static final Registry<MapCodec<? extends TextureExtractor>> TEXTURE_EXTRACTOR_TYPE = register(LockoutRegistryKeys.TEXTURE_EXTRACTOR_TYPE);
    public static final Registry<MapCodec<? extends GoalDifficultySupplier<?>>> DIFFICULTY_SUPPLIER_TYPE = register(LockoutRegistryKeys.DIFFICULTY_SUPPLIER_TYPE);
    public static final Registry<MapCodec<? extends GoalRequirement>> GOAL_REQUIREMENT_TYPE = register(LockoutRegistryKeys.GOAL_REQUIREMENT_TYPE);
    public static final Registry<MapCodec<? extends ServerHint<?>>> SERVER_HINT_TYPE = register(LockoutRegistryKeys.SERVER_HINT_TYPE);
    public static final Registry<MapCodec<? extends ClientHint<?>>> CLIENT_HINT_TYPE = register(LockoutRegistryKeys.CLIENT_HINT_TYPE);

    public static void initialize() {
        DifficultyCheckFunction.bootstrap(DIFFICULTY_CHECK_TYPE);
        DifficultyCheckFunction.Modifier.bootstrap(DIFFICULTY_CHECK_MODIFIER_TYPE);
        TextureExtractor.bootstrap(TEXTURE_EXTRACTOR_TYPE);
        GoalDifficultySupplier.bootstrap(DIFFICULTY_SUPPLIER_TYPE);
        GoalRequirement.bootstrap(GOAL_REQUIREMENT_TYPE);
        ServerHint.bootstrap(SERVER_HINT_TYPE);
        ClientHint.bootstrap(CLIENT_HINT_TYPE);
    }

    public static <T> T register(Registry<T> registry, String name, T value) {
        return Registry.register(registry, Constants.id(name), value);
    }

    private static <T> Registry<T> register(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key).buildAndRegister();
    }
}

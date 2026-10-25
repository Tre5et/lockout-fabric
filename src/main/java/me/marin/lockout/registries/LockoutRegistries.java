package me.marin.lockout.registries;

import com.mojang.serialization.MapCodec;
import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class LockoutRegistries {
    public static final Registry<MapCodec<? extends DifficultyCheckFunction>> DIFFICULTY_CHECK_TYPE = register(LockoutRegistryKeys.DIFFICULTY_CHECK_TYPE);
    public static final Registry<MapCodec<? extends DifficultyCheckFunction.Modifier>> DIFFICULTY_CHECK_MODIFIER_TYPE = register(LockoutRegistryKeys.DIFFICULTY_CHECK_MODIFIER_TYPE);

    public static void initialize() {
        DifficultyCheckFunction.bootstrap(DIFFICULTY_CHECK_TYPE);
        DifficultyCheckFunction.Modifier.bootstrap(DIFFICULTY_CHECK_MODIFIER_TYPE);
    }

    public static <T> T register(Registry<T> registry, String name, T value) {
        return Registry.register(registry, Constants.id(name), value);
    }

    private static <T> Registry<T> register(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key).buildAndRegister();
    }
}

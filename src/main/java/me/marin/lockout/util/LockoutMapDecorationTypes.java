package me.marin.lockout.util;

import me.marin.lockout.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class LockoutMapDecorationTypes {
    public static final Holder<MapDecorationType> SULFUR_CAVE = register("sulfur_caves",  "sulfur_caves", true, false);
    public static final Holder<MapDecorationType> JUNGLE = register("jungle",  "jungle", true, false);
    public static final Holder<MapDecorationType> DESERT = register("desert",  "desert", true, false);
    public static final Holder<MapDecorationType> TAIGA = register("taiga",  "taiga", true, false);
    public static final Holder<MapDecorationType> SWAMP = register("swamp",  "swamp", true, false);

    public static Holder<MapDecorationType> bootstrap() {
        return SULFUR_CAVE;
    }

    private static Holder<MapDecorationType> register(String name, String assetName, boolean showOnItemFrame, boolean trackCount) {
        ResourceKey<MapDecorationType> key = ResourceKey.create(Registries.MAP_DECORATION_TYPE, Identifier.fromNamespaceAndPath(Constants.NAMESPACE, name));
        MapDecorationType type = new MapDecorationType(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, assetName), showOnItemFrame, trackCount);
        return Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, key, type);
    }
}

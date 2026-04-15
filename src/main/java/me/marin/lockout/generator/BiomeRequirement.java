package me.marin.lockout.generator;

import me.marin.lockout.LocateData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import java.util.Map;

public interface BiomeRequirement {

    boolean isMet(Map<ResourceKey<Biome>, LocateData> biomes);

    void collectBiomes(java.util.Collection<ResourceKey<Biome>> collector);

}

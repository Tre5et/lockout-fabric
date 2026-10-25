package me.marin.lockout.server.goal.hint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import oshi.util.tuples.Pair;

import java.util.List;

public class BiomeServerHint extends PositionServerHint {
    public static final MapCodec<BiomeServerHint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(BiomeServerHint::getName),
            MapDecorationType.CODEC.fieldOf("decoration").forGetter(BiomeServerHint::getDecoration),
            Codec.list(Level.RESOURCE_KEY_CODEC).optionalFieldOf("levels", List.of(Level.OVERWORLD)).forGetter(BiomeServerHint::getApplicableLevels),
            Codec.list(ResourceKey.codec(Registries.BIOME)).fieldOf("biomes").forGetter(BiomeServerHint::getAcceptableBiomes)
    ).apply(instance, BiomeServerHint::new));

    @Getter
    private final List<ResourceKey<Biome>> acceptableBiomes;

    public BiomeServerHint(String name, Holder<MapDecorationType> decoration, List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Biome>> acceptableBiomes) {
        super(name, decoration, applicableLevels);
        this.acceptableBiomes = acceptableBiomes;
    }

    @Override
    protected GoalHintResult<Pair<BlockPos, Identifier>> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level) {
        com.mojang.datafixers.util.Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
                biomeRegistryEntry -> acceptableBiomes.stream().anyMatch(biomeRegistryEntry::is),
                player.blockPosition(),
                10_000,
                32,
                64
        );

        if(found == null) {
            return GoalHintResult.error("Failed to find the biome.");
        }

        return GoalHintResult.result(new Pair<>(found.getFirst(), level.dimension().identifier()));
    }

    @Override
    public MapCodec<? extends ServerHint<?>> codec() {
        return MAP_CODEC;
    }
}

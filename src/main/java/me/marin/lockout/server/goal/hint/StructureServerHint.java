package me.marin.lockout.server.goal.hint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.marin.lockout.lockout.goal.hint.GoalHintResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import oshi.util.tuples.Pair;

import java.util.List;

public class StructureServerHint extends PositionServerHint {
    public static final MapCodec<StructureServerHint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(StructureServerHint::getName),
            MapDecorationType.CODEC.fieldOf("decoration").forGetter(StructureServerHint::getDecoration),
            Codec.list(Level.RESOURCE_KEY_CODEC).optionalFieldOf("levels", List.of(Level.OVERWORLD)).forGetter(StructureServerHint::getApplicableLevels),
            Codec.list(ResourceKey.codec(Registries.STRUCTURE)).fieldOf("structures").forGetter(StructureServerHint::getAcceptableStructures)
    ).apply(instance, StructureServerHint::new));

    @Getter
    private final List<ResourceKey<Structure>> acceptableStructures;

    public StructureServerHint(String name, Holder<MapDecorationType> decoration, List<ResourceKey<Level>> applicableLevels, List<ResourceKey<Structure>> acceptableStructures) {
        super(name, decoration, applicableLevels);
        this.acceptableStructures = acceptableStructures;
    }

    @Override
    protected GoalHintResult<Pair<BlockPos, Identifier>> resolveWithLevel(MinecraftServer server, ServerPlayer player, ServerLevel level) {
        com.mojang.datafixers.util.Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(acceptableStructures.stream().map(level.registryAccess().lookupOrThrow(Registries.STRUCTURE)::getOrThrow).toList()),
                player.blockPosition(),
                10_000,
                false
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

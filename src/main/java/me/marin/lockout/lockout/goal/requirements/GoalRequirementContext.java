package me.marin.lockout.lockout.goal.requirements;

import me.marin.lockout.LocateData;
import me.marin.lockout.server.ServerLockoutTeam;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Map;

public record GoalRequirementContext(
        Map<ResourceKey<Biome>, LocateData> biomes,
        Map<ResourceKey<Structure>, LocateData> structures,
        List<ServerLockoutTeam> teams
) {
    public static GoalRequirementContext EMPTY = new GoalRequirementContext(Map.of(), Map.of(), List.of());
}

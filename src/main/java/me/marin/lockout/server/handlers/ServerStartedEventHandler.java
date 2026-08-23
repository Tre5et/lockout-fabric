package me.marin.lockout.server.handlers;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContextInitializer;
import me.marin.lockout.server.LockoutServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import static me.marin.lockout.server.LockoutServer.*;

public class ServerStartedEventHandler implements ServerLifecycleEvents.ServerStarted {

    @Override
    public void onServerStarted(MinecraftServer server) {
        server.execute(() -> {
            Lockout.log("Locating all required Structures and Biomes");
            LockoutServer.server = server;
            long start = System.currentTimeMillis();

            GoalRequirementContext context = new GoalRequirementContextInitializer.Combined(
                    GoalRegistry.INSTANCE.getRegisteredGoals().stream()
                            .flatMap(g -> g.getRequirements().stream().map(GoalRequirement::getInitializer))
                            .toList()
            ).initialize(server);

/*            for (GoalBuilder<?> goal : GoalRegistry.INSTANCE.getRegisteredGoals()) {
                GoalRequirements goalRequirements = goal.getRequirements();
                if (goalRequirements == null) continue;

                java.util.Set<ResourceKey<Biome>> biomesToLocate = new java.util.HashSet<>(goalRequirements.getRequiredBiomes());

                if (goalRequirements.getBiomeRequirement() != null) {
                    goalRequirements.getBiomeRequirement().collectBiomes(biomesToLocate);
                }

                for (ResourceKey<Biome> biome : biomesToLocate) {
                    locateBiome(server, biome);
                }

                for (ResourceKey<Structure> structure : goalRequirements.getRequiredStructures()) {
                    locateStructure(server, structure);
                }
            }*/
            long end = System.currentTimeMillis();
            Lockout.log("Located " + context.biomes().size() + " biomes and " + context.structures().size() + " structures in " + String.format("%.2f", ((end-start)/1000.0)) + "s!");

            CONTEXT = context;

            // Freeze ticks until lockout/blackout game starts
            var freezeCommand = "tick freeze";
            var parseResults = server.getCommands().getDispatcher().parse(freezeCommand, server.createCommandSourceStack());
            server.getCommands().performCommand(parseResults, freezeCommand);
        });
    }
}

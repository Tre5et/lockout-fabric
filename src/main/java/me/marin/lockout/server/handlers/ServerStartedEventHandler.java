package me.marin.lockout.server.handlers;

import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutRunnable;
import me.marin.lockout.lockout.GoalRegistry;
import me.marin.lockout.lockout.goal.requirements.GoalRequirement;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContextInitializer;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Path;

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

            long end = System.currentTimeMillis();
            Lockout.log("Located " + context.biomes().size() + " biomes and " + context.structures().size() + " structures in " + String.format("%.2f", ((end-start)/1000.0)) + "s!");

            CONTEXT = context;

            // Freeze ticks until lockout/blackout game starts
            server.tickRateManager().setFrozen(true);

            try {
                ServerLockoutGame lockout = ServerLockoutGame.load(Path.of(server.getWorldPath(LevelResource.DATA).toAbsolutePath().toString(), "lockout", "game.json"));
                if(lockout != null) {
                    LockoutServer.lockout = lockout;
                    if(lockout.getTicks() >= 0) {
                        server.tickRateManager().setFrozen(false);
                    } else {
                        ((LockoutRunnable)LockoutServer::startLockoutRunning).runTaskAfter(-lockout.getTicks());
                    }
                }
                Lockout.log("Loaded lockout game state.");
            } catch (IOException e) {
                Lockout.log("Failed to load lockout game state: " + e.getMessage());
            }
        });
    }
}

package me.marin.lockout.mixin.server;

import me.marin.lockout.game.LockoutGame;
import me.marin.lockout.lockout.goal.builder.entity.EntityUtil;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinBlockMixin {
    @Inject(method = "spawnGolemInWorld", at = @At("HEAD"))
    private static void onGolemSpawn(final Level level, final BlockPattern.BlockPatternMatch match, final Entity golem, final BlockPos spawnPos, CallbackInfo ci) {
        if(level.isClientSide()) return;
        ServerLockoutGame game = LockoutServer.lockout;
        if(!LockoutGame.isActive(game)) return;

        Optional<? extends Player> nearestPlayer = level.players().stream()
                .min((p1, p2) -> {
                    double dist1 = p1.blockPosition().distSqr(spawnPos);
                    double dist2 = p2.blockPosition().distSqr(spawnPos);
                    return Double.compare(dist1, dist2);
                });
        if(nearestPlayer.isEmpty()) return;
        game.getBoard().update(new EntityUtil.PlayerSpawnedEntity(golem.getType()), nearestPlayer.get());
    }

    /*@Inject(method = "trySpawnGolem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/golem/CopperGolem;spawn(Lnet/minecraft/world/level/block/WeatheringCopper$WeatherState;)V"))
    public void onCopperGolemSpawn(Level world, BlockPos pos, CallbackInfo ci) {
        if (world.isClientSide()) return;

        Lockout lockout = LockoutServer.lockout;
        if (!Lockout.isLockoutRunning(lockout)) return;

        lockout$onCopperGolemSpawn(lockout, (ServerLevel) world, pos);
    }

    @Unique
    private static void lockout$onCopperGolemSpawn(Lockout lockout, ServerLevel world, BlockPos pos) {
        // Find the nearest player to the copper golem spawn location
        var players = world.players();
        if (players.isEmpty()) return;

        var nearestPlayer = players.stream()
                .min((p1, p2) -> {
                    double dist1 = p1.blockPosition().distSqr(pos);
                    double dist2 = p2.blockPosition().distSqr(pos);
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);

        if (nearestPlayer == null) return;

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.isCompleted()) continue;

            if (goal instanceof ConstructCopperGolemGoal) {
                lockout.completeGoal(goal, nearestPlayer);
            }
        }
    }*/
}

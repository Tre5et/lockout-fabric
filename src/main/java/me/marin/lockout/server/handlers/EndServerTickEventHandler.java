package me.marin.lockout.server.handlers;

import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutRunnable;
import me.marin.lockout.lockout.goal.Goal;
import me.marin.lockout.lockout.goal.ObtainItemGoal;
import me.marin.lockout.lockout.goal.RideEntityGoal;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;

import static me.marin.lockout.server.LockoutServer.gameStartRunnables;
import static me.marin.lockout.server.LockoutServer.lockout;

public class EndServerTickEventHandler implements ServerTickEvents.EndTick {

    @Override
    public void onEndTick(MinecraftServer server) {
        if (!Lockout.isLockoutRunning(lockout)) return;

        for (LockoutRunnable runnable : new HashSet<>(gameStartRunnables.keySet())) {
            if (gameStartRunnables.get(runnable) <= 0) {
                runnable.run();
                gameStartRunnables.remove(runnable);
            } else {
                gameStartRunnables.merge(runnable, -1L, Long::sum);
            }
        }

        for (Goal goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;

            /*if (goal instanceof HaveMostXPLevelsGoal) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    lockout.levels.put(player.getUUID(), player.isDeadOrDying() ? 0 : player.experienceLevel);
                }
                lockout.recalculateXPGoal(goal);
            }

            if (goal instanceof HaveMostHoppersGoal) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!lockout.isLockoutPlayer(player.getUUID())) continue;
                    
                    int hopperCount = player.getInventory().countItem(Items.HOPPER);
                    lockout.playerHopperCounts.put(player.getUUID(), hopperCount);
                }
                lockout.recalculateHoppersGoal(goal);
            }

            if (goal instanceof HaveMostLeaflitterGoal) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!lockout.isLockoutPlayer(player.getUUID())) continue;
                    
                    int leaflitterCount = player.getInventory().countItem(Items.LEAF_LITTER);
                    lockout.playerLeaflitterCounts.put(player.getUUID(), leaflitterCount);
                }
                lockout.recalculateLeaflitterGoal(goal);
            }

            if (goal instanceof HaveMostDiamondBlocksGoal) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!lockout.isLockoutPlayer(player.getUUID())) continue;
                    
                    int diamondBlockCount = player.getInventory().countItem(Items.DIAMOND_BLOCK);
                    lockout.playerDiamondBlockCounts.put(player.getUUID(), diamondBlockCount);
                }
                lockout.recalculateDiamondBlocksGoal(goal);
            }*/

            if (goal.isCompleted()) continue;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (goal instanceof ObtainItemGoal obtainItemsGoal) {
                    if (obtainItemsGoal.satisfiedBy(player.getInventory())) {
/*                        if (goal instanceof OpponentObtainsItemGoal opponentObtainsItemGoal) {
                            lockout.completeMultiOpponentGoal(goal, player, opponentObtainsItemGoal.getMessage(player));
                        } else {*/
                            lockout.completeGoal(goal, player);
/*                        }*/
                    }
                }

                if (goal instanceof RideEntityGoal rideEntityGoal && player.isPassenger()) {
                    EntityType<?> vehicle = player.getVehicle().getType();
                    if(rideEntityGoal.satisfiedBy(vehicle)) {
                        lockout.completeGoal(goal, player);
                    }
/*                    if (Objects.equals(vehicle, rideEntityGoal.getEntityType()) || (rideEntityGoal.getEntityType() == EntityTypes.NAUTILUS && vehicle == EntityTypes.ZOMBIE_NAUTILUS)) {
                        boolean allow = true;
                        if (Objects.equals(vehicle, EntityTypes.PIG)) {
                            boolean hasCarrotOnAStick = false;
                            var handItem = player.getInventory().getSelectedItem();
                            if (handItem.getItem().equals(Items.CARROT_ON_A_STICK)) {
                                hasCarrotOnAStick = true;
                            }
                            allow = hasCarrotOnAStick;
                        }
                        if (allow) {
                            lockout.completeGoal(goal, player);
                        }
                    }*/
                }
/*                if (goal instanceof EmptyHungerBarGoal) {
                    if (player.getFoodData().getFoodLevel() == 0) {
                        lockout.completeGoal(goal, player);
                    }
                }
                if (goal instanceof ReachHeightLimitGoal) {
                    if (player.getY() >= 320 && player.level().dimension() == ServerLevel.OVERWORLD) {
                        lockout.completeGoal(goal, player);
                    }
                }
                if (goal instanceof ReachNetherRoofGoal) {
                    if (player.getY() >= 128 && player.level().dimension() == ServerLevel.NETHER) {
                        lockout.completeGoal(goal, player);
                    }
                }
                if (goal instanceof ReachBedrockGoal) {
                    if (player.getY() < 10 && Objects.equals(player.level().getBlockState(player.blockPosition().below()).getBlock(), Blocks.BEDROCK)) {
                        lockout.completeGoal(goal, player);
                    }
                }
                if (goal instanceof OpponentTouchesWaterGoal) {
                    if (Objects.equals(player.level().getBlockState(player.blockPosition()).getBlock(), Blocks.WATER)) {
                        lockout.completeMultiOpponentGoal(goal, player, player.getName().getString() + " touched water.");
                    }
                }*/
            }
        }

        lockout.tick();
        if (lockout.getTicks() % 20 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, lockout.getUpdateTimerPacket());
            }
        }
    }
}

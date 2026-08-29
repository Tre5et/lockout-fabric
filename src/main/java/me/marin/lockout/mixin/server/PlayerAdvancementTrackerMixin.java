package me.marin.lockout.mixin.server;

import me.marin.lockout.server.ServerLockoutTeam;
import me.marin.lockout.server.LockoutServer;
import me.marin.lockout.server.game.ServerLockoutGame;
import me.marin.lockout.server.goal.ServerGoal;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    private ServerPlayer player;

    @Redirect(method = "lambda$award$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V") )
    public void onBroadcastInChat(PlayerList instance, Component message, boolean overlay) {
        ServerLockoutGame lockout = LockoutServer.lockout;

        // Prevent spectator advancements from showing in chat
        if (lockout == null || !lockout.getState().isActive() || lockout.isLockoutPlayer(player.getUUID())) {
            instance.broadcastSystemMessage(message, overlay);
        }
    }

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/Advancement;rewards()Lnet/minecraft/advancements/AdvancementRewards;") )
    public void onGrantCriterion(AdvancementHolder advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (lockout == null || !lockout.getState().isActive()) return;
        if (!lockout.isLockoutPlayer(player.getUUID())) return;
        ServerLockoutTeam team = (ServerLockoutTeam) lockout.getPlayerTeam(player.getUUID());

        lockout.getBoard().update(advancement, player, true);
    }

    private static final Identifier ADVENTURING_TIME = Identifier.fromNamespaceAndPath("minecraft", "adventure/adventuring_time");
    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;isDone()Z", ordinal = 1, shift = At.Shift.BEFORE) )
    public void onAdvancementProgress(AdvancementHolder advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        ServerLockoutGame lockout = LockoutServer.lockout;
        if (lockout == null || !lockout.getState().isActive()) return;

        if (!advancement.id().equals(ADVENTURING_TIME)) return;
        Identifier biomeId = Identifier.parse(criterionName);

        for (ServerGoal<?> goal : lockout.getBoard().getGoals()) {
            if (goal == null) continue;
            if (goal.hasAnyCompleted()) continue;

            /*if (goal instanceof VisitBiomeGoal visitBiomeGoal) {
                if (visitBiomeGoal.getBiomes().contains(biomeId)) {
                    lockout.completeGoal(goal, player);
                }
            }

            if (goal instanceof me.marin.lockout.lockout.interfaces.VisitUniqueBiomesGoal visitUniqueBiomesGoal) {
                LockoutTeamServer team = (LockoutTeamServer) lockout.getPlayerTeam(player.getUUID());
                lockout.visitedBiomes.computeIfAbsent(team, t -> new LinkedHashSet<>());
                lockout.visitedBiomes.get(team).add(biomeId);

                int size = lockout.visitedBiomes.get(team).size();

                team.sendTooltipUpdate(visitUniqueBiomesGoal);
                if (size >= visitUniqueBiomesGoal.getAmount()) {
                    lockout.completeGoal(goal, team);
                }
            }*/
        }

    }
}

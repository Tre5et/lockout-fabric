package me.marin.lockout.lockout.goal;

import me.marin.lockout.Lockout;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.lockout.goal.hint.GoalHint;
import me.marin.lockout.lockout.goal.progress.GoalProgressTracker;
import me.marin.lockout.lockout.goal.rendering.name.NameExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.network.GoalProgressPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class ProgressingGoal<D,T,R,P extends GoalProgressTracker<T,R>> extends Goal {
    private final P progressTracker;
    private final Predicate<R> satisfiedPredicate;

    public ProgressingGoal(String id, NameExtractor nameExtractor, TextureExtractor textureExtractor, List<GoalHint> hints, Pair<String, String> buildData, P progressTracker, Predicate<R> satisfiedPredicate) {
        super(id, nameExtractor, textureExtractor, hints, buildData);
        this.progressTracker = progressTracker;
        this.satisfiedPredicate = satisfiedPredicate;
    }

    public boolean satisfiedBy(Player player, D data, Lockout lockout) {
        Optional<? extends LockoutTeam> team = lockout.getTeams().stream()
                .filter(t -> t.containsPlayer(player.getUUID()))
                .findAny();
        if(team.isEmpty()) return false;
        int index = lockout.getTeams().indexOf(team.get());
        T updateData = getUpdateData(data, progressTracker.get(index));
        if(updateData == null) return false;
        progressTracker.update(index, updateData);
        lockout.broadcastProgress(this, progressTracker.serialize());
        return satisfiedPredicate.test(progressTracker.get(index));
    }

    @Override
    public void setProgress(String progress) {
        progressTracker.set(progress);
    }

    @Override
    public void sendProgress(ServerPlayer player) {
        ServerPlayNetworking.send(player, new GoalProgressPayload(getId(), progressTracker.serialize()));
    }

    @Override
    public List<Component> getTooltip(LockoutTeam team, Player player, Lockout lockout) {
        return progressTracker.getTooltip(team, player, lockout);
    }

    @Override
    public List<Component> getSpectatorTooltip(Lockout lockout) {
        return progressTracker.getSpectatorTooltip(lockout);
    }

    protected abstract T getUpdateData(D data, R progress);
}

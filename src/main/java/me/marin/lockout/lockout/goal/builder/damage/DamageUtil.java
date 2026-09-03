package me.marin.lockout.lockout.goal.builder.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class DamageUtil {
    public record DealtDamage(
            float damage
    ) {}

    public record PlayerDied(
            DamageSource source,
            Player player
    ) {}

    public record KilledEntity(
            Entity entity,
            DamageSource source
    ) {}
}

package me.marin.lockout.lockout.goal.builder.damage;

import net.minecraft.world.damagesource.DamageSource;

public class DamageUtil {
    public record DealtDamage(
            float damage
    ) {}

    public record PlayerDied(
            DamageSource source
    ) {}
}

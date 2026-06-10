package io.redspace.ironsjewelry.core.actions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class IgnorableExplosion extends ServerExplosion {
    final boolean affectOwner;
    final UUID owner;

    public IgnorableExplosion(
            ServerLevel level,
            boolean affectOwner,
            Entity owner,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            Vec3 center,
            float radius,
            boolean fire,
            Explosion.BlockInteraction blockInteraction
    ) {
        super(level, source, damageSource, damageCalculator, center, radius, fire, blockInteraction);
        this.affectOwner = affectOwner;
        this.owner = owner.getUUID();
    }

    public boolean shouldIgnore(Entity entity) {
        return !affectOwner && entity.getUUID().equals(owner);
    }
}

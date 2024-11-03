package io.redspace.ironsjewelry.core.actions;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class IgnorableExplosion extends Explosion {
    final boolean affectOwner;
    final UUID owner;

    public IgnorableExplosion(
            Level pLevel,
            boolean affectOwner,
            Entity owner,
            @Nullable Entity pSource,
            @Nullable DamageSource pDamageSource,
            @Nullable ExplosionDamageCalculator pDamageCalculator,
            double pX,
            double pY,
            double pZ,
            float pRadius,
            boolean pFire,
            Explosion.BlockInteraction pBlockInteraction,
            ParticleOptions pSmallExplosionParticles,
            ParticleOptions pLargeExplosionParticles,
            Holder<SoundEvent> pExplosionSound
    ) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pX, pY, pZ, pRadius, pFire, pBlockInteraction, pSmallExplosionParticles, pLargeExplosionParticles, pExplosionSound);
        this.affectOwner = affectOwner;
        this.owner = owner.getUUID();
    }

    public boolean shouldIgnore(Entity entity) {
        return !affectOwner && entity.getUUID().equals(owner);
    }
}

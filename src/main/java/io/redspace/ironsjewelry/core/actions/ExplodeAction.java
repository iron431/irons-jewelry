package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public record ExplodeAction(
        boolean affectSelf,
        boolean attributeToUser,
        String translation,
        Optional<Holder<DamageType>> damageType,
        Optional<QualityScalar> knockbackMultiplier,
        Optional<HolderSet<Block>> immuneBlocks,
        Vec3 offset,
        QualityScalar radius,
        boolean createFire,
        Level.ExplosionInteraction blockInteraction,
        ParticleOptions smallParticle,
        ParticleOptions largeParticle,
        Holder<SoundEvent> sound
) implements IAction {
    public static final MapCodec<ExplodeAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("affect_self", true).forGetter(ExplodeAction::affectSelf),
            Codec.BOOL.optionalFieldOf("attribute_to_user", false).forGetter(ExplodeAction::attributeToUser),
            Codec.STRING.fieldOf("translation").forGetter(ExplodeAction::translation),
            DamageType.CODEC.optionalFieldOf("damage_type").forGetter(ExplodeAction::damageType),
            QualityScalar.CODEC.optionalFieldOf("knockback_multiplier").forGetter(ExplodeAction::knockbackMultiplier),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(ExplodeAction::immuneBlocks),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(ExplodeAction::offset),
            QualityScalar.CODEC.fieldOf("radius").forGetter(ExplodeAction::radius),
            Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplodeAction::createFire),
            Level.ExplosionInteraction.CODEC.fieldOf("block_interaction").forGetter(ExplodeAction::blockInteraction),
            ParticleTypes.CODEC.fieldOf("small_particle").forGetter(ExplodeAction::smallParticle),
            ParticleTypes.CODEC.fieldOf("large_particle").forGetter(ExplodeAction::largeParticle),
            SoundEvent.CODEC.fieldOf("sound").forGetter(ExplodeAction::sound)

    ).apply(builder, ExplodeAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        Vec3 origin = (applyToSelf ? wearer : entity).position().add(serverLevel.random.nextDouble() * .1 - .05, serverLevel.random.nextDouble() * .2 + .2, serverLevel.random.nextDouble() * .1 - .05);
        Vec3 vec3 = origin.add(this.offset);
        var exRadius = Math.max((float) this.radius.sample(quality), 0.0F);
        Explosion explosion = new IgnorableExplosion(
                serverLevel,
                affectSelf,
                wearer,
                this.attributeToUser ? wearer : null,
                this.getDamageSource(wearer, vec3),
                new SimpleExplosionDamageCalculator(
                        this.blockInteraction != Level.ExplosionInteraction.NONE,
                        this.damageType.isPresent(),
                        this.knockbackMultiplier.map(p_345018_ -> (float) p_345018_.sample(quality)),
                        this.immuneBlocks
                ),
                vec3.x(),
                vec3.y(),
                vec3.z(),
                exRadius,
                this.createFire,
                Explosion.BlockInteraction.KEEP,//explosion$blockinteraction,
                smallParticle,
                largeParticle,
                sound
        );
        if (net.neoforged.neoforge.event.EventHooks.onExplosionStart(serverLevel, explosion)) {
            return;
        }
        explosion.explode();
        explosion.finalizeExplosion(true);
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer serverplayer : serverLevel.getPlayers(serverplayer -> serverplayer.distanceToSqr(vec3) < 4096.0)) {
            serverplayer.connection
                    .send(
                            new ClientboundExplodePacket(
                                    vec3.x,
                                    vec3.y,
                                    vec3.z,
                                    exRadius,
                                    explosion.getToBlow(),
                                    explosion.getHitPlayers().get(serverplayer),
                                    explosion.getBlockInteraction(),
                                    explosion.getSmallExplosionParticles(),
                                    explosion.getLargeExplosionParticles(),
                                    explosion.getExplosionSound()
                            )
                    );
        }
    }

    @Nullable
    private DamageSource getDamageSource(Entity pEntity, Vec3 pPos) {
        if (this.damageType.isEmpty()) {
            return null;
        } else {
            return this.attributeToUser ? new DamageSource(this.damageType.get(), pEntity) : new DamageSource(this.damageType.get(), pPos);
        }
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        String string = applyToSelf ? "action.irons_jewelry.explode.self" : "action.irons_jewelry.explode";
        return Component.translatable(string, Component.translatable(this.translation).withStyle(ChatFormatting.RED), Utils.stringTruncation(radius.sample(bonusInstance.quality()), 1));
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}

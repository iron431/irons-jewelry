package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.registry.AttributeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber
public class AttributeImplEvents {

    @SubscribeEvent
    public static void handleDamageAttributes(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }

        /* *******************
         * Victim Effects
         ******************** */

        // dodge chance
        if (target.getRandom().nextDouble() < target.getAttributeValue(AttributeRegistry.DODGE_CHANCE) - 1) {
            event.setCanceled(true);
            target.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, target.getSoundSource(), 1.5f, 2f);
            Utils.spawnParticles(target.level(), ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY() + target.getBbHeight() * 0.5f, target.getZ(), 15, 0.2, 0.4, 0.2, 0.01, false);
            target.invulnerableTime = 20;
            return;
        }

        /* *******************
         * Attacker Effects
         ******************** */
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        float amount = event.getAmount();

        // arrow damage
        if (event.getSource().getDirectEntity() instanceof AbstractArrow) {
            double arrowDamage = attacker.getAttributeValue(AttributeRegistry.ARROW_DAMAGE);
            if (arrowDamage != 1) {
                amount *= (float) arrowDamage;
            }
        }

        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void handleCritMultiplier(CriticalHitEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        double critDamage = event.getEntity().getAttributeValue(AttributeRegistry.CRIT_DAMAGE);
        if (critDamage != 1) {
            event.setDamageMultiplier((float) (event.getDamageMultiplier() + critDamage - 1));
        }
    }

    @SubscribeEvent
    public static void handleBreakSpeed(PlayerEvent.BreakSpeed event) {
        double miningSpeed = event.getEntity().getAttributeValue(AttributeRegistry.MINING_SPEED);
        if (miningSpeed != 1) {
            event.setNewSpeed((float) (event.getNewSpeed() * miningSpeed));
        }
    }

    @SubscribeEvent
    public static void handleBlockXpGained(BlockDropsEvent event) {
        int xp = event.getDroppedExperience();
        if (xp > 0 && event.getBreaker() != null) {
            event.setDroppedExperience(applyBonusXp(event.getBreaker(), xp));
        }
    }

    @SubscribeEvent
    public static void handleMobXpGained(LivingExperienceDropEvent event) {
        int xp = event.getDroppedExperience();
        if (xp > 0 && event.getAttackingPlayer() != null) {
            event.setDroppedExperience(applyBonusXp(event.getAttackingPlayer(), xp));
        }
    }

    @SubscribeEvent
    public static void handleHealingReceived(LivingHealEvent event) {
        double healingReceived = event.getEntity().getAttributeValue(AttributeRegistry.HEALING_RECEIVED);
        if (healingReceived != 1) {
            event.setAmount((float) (event.getAmount() * healingReceived));
        }
    }

    private static int applyBonusXp(Entity entity, int baseXp) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return baseXp;
        }
        double bonusXp = livingEntity.getAttributeValue(AttributeRegistry.EXPERIENCE_GAINED);
        if (bonusXp == 1) {
            return baseXp;
        }
        return (int) Math.max(0, baseXp * bonusXp + 0.5);
    }
}

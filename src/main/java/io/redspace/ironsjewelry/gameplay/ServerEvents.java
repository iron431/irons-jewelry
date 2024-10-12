package io.redspace.ironsjewelry.gameplay;

import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.bonuses.DeathBonus;
import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerDataPacket(serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA)));
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlocked() && event.getEntity() instanceof ServerPlayer player && event.getDamageSource().getEntity() instanceof LivingEntity livingAttacker) {
            var items = Utils.getEquippedJewelry(player);
            for (ItemStack stack : items) {
                JewelryData.ifPresent(stack, jewelryData -> {
                    jewelryData.forBonuses(BonusRegistry.ON_SHIELD_BLOCK_BONUS.get(), ActionParameter.ActionRunnable.class, (bonus, action) -> {
                        action.action().apply(player.serverLevel(), bonus.quality(), action.targetSelf(), player, livingAttacker);
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamaged(LivingIncomingDamageEvent event) {
        var damageSource = event.getSource();
        var attacker = event.getSource().getEntity();
        var victim = event.getEntity();
        /*
        Attacker Effects
         */
        if (attacker instanceof Player player) {
            var bonuses = Utils.getEquippedBonuses(player);
            for (BonusInstance instance : bonuses) {
                /*
                Effect on projectile hit
                 */
                if (instance.bonus().equals(BonusRegistry.EFFECT_ON_PROJECTILE_HIT_BONUS.get())) {
                    if (damageSource.getDirectEntity() instanceof Projectile) {
                        BonusRegistry.EFFECT_ON_PROJECTILE_HIT_BONUS.get().getParameterType().resolve(instance).ifPresent(
                                effect -> victim.addEffect(new MobEffectInstance(effect, BonusRegistry.EFFECT_ON_PROJECTILE_HIT_BONUS.get().durationInTicks(effect, instance.quality())))
                        );
                    }
                }
            }
        }
        /*
        Victim Effects
         */
        if (victim instanceof Player player) {
            for (BonusInstance instance : Utils.getEquippedBonuses(player)) {
                if (instance.bonus() instanceof DeathBonus) {
                    player.die(player.level().damageSources().fellOutOfWorld());
                } else if (instance.bonus() instanceof EffectOnHitBonus effectOnHitBonus) {
                    effectOnHitBonus.getParameterType().resolve(instance.parameter()).ifPresent(effect ->
                            player.addEffect(new MobEffectInstance(effect, effectOnHitBonus.durationInTicks(effect, instance.quality())))
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectApplication(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            Utils.getEquippedJewelry(player).forEach(stack ->
                    JewelryData.ifPresent(stack, data -> data.forBonuses(BonusRegistry.EFFECT_IMMUNITY_BONUS.get(), Holder.class,
                            (bonus, param) -> {
                                if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().equals(param)) {
                                    event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                                }
                            }))
            );
        }
    }
}

package io.redspace.ironsjewelry.gameplay;

import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.bonuses.DeathBonus;
import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
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
}

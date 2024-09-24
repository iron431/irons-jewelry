package io.redspace.ironsjewelry.gameplay;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.bonuses.DeathBonus;
import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            IronsJewelry.LOGGER.debug("player took damage!!");
            ItemStack tempJewelry = player.getMainHandItem();
            var data = tempJewelry.get(ComponentRegistry.JEWELRY_COMPONENT);
            if (data != null) {
                for (BonusInstance instance : data.getBonuses()) {
                    if (instance.bonus() instanceof DeathBonus) {
                        player.die(player.level().damageSources().fellOutOfWorld());
                    } else if (instance.bonus() instanceof EffectOnHitBonus effectOnHitBonus) {
                        effectOnHitBonus.getParameter().resolve(instance.parameter()).ifPresent(effect ->
                                player.addEffect(new MobEffectInstance(effect, 100))
                        );
                    }
                }
            }
        }
    }
}

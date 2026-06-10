package io.redspace.ironsjewelry.utils;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber
public class DamageHelper {
    private static final HashMap<UUID, Integer> knockbackImmunes = new HashMap<>();

    public static void ignoreNextKnockback(LivingEntity livingEntity) {
        if (livingEntity.level().getServer() != null) {
            var tickCount = livingEntity.level().getServer().getTickCount();
            //garbage collect
            knockbackImmunes.entrySet().stream().filter(entry -> tickCount - entry.getValue() >= 10).toList().forEach(entry -> knockbackImmunes.remove(entry.getKey()));
            //enter entity
            knockbackImmunes.put(livingEntity.getUUID(), tickCount);
        }
    }

    @SubscribeEvent
    public static void cancelKnockback(LivingKnockBackEvent event) {
        //IronsSpellbooks.LOGGER.debug("DamageSources.cancelKnockback {}", event.getEntity().getName().getString());
        var entity = event.getEntity();
        if (entity.level().getServer() != null && knockbackImmunes.containsKey(event.getEntity().getUUID())) {
            if (entity.level().getServer().getTickCount() - knockbackImmunes.get(entity.getUUID()) <= 1) {
                event.setCanceled(true);
            }
            knockbackImmunes.remove(entity.getUUID());
        }
    }
}

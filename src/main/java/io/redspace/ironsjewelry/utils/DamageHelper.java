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
    private static final ThreadLocal<Integer> jewelryActionDepth = ThreadLocal.withInitial(() -> 0);

    public static boolean isHandlingJewelryAction() {
        return jewelryActionDepth.get() > 0;
    }

    public static void runWithoutRecursiveBonusTriggers(Runnable runnable) {
        // Bonus actions can deal damage synchronously, which would otherwise re-enter the bonus event pipeline.
        jewelryActionDepth.set(jewelryActionDepth.get() + 1);
        try {
            runnable.run();
        } finally {
            int depth = jewelryActionDepth.get() - 1;
            if (depth <= 0) {
                jewelryActionDepth.remove();
            } else {
                jewelryActionDepth.set(depth);
            }
        }
    }

    public static void ignoreNextKnockback(LivingEntity livingEntity) {
        if (livingEntity.getServer() != null) {
            var tickCount = livingEntity.getServer().getTickCount();
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
        if (entity.getServer() != null && knockbackImmunes.containsKey(event.getEntity().getUUID())) {
            if (entity.getServer().getTickCount() - knockbackImmunes.get(entity.getUUID()) <= 1) {
                event.setCanceled(true);
            }
            knockbackImmunes.remove(entity.getUUID());
        }
    }
}

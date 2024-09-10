package io.redspace.ironsjewelry.gameplay;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.bonuses.DeathBonus;
import io.redspace.ironsjewelry.core.data.BonusSource;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            IronsJewelry.LOGGER.debug("player took damage!!");
            ItemStack tempJewelry = player.getMainHandItem();
            var data = tempJewelry.get(ComponentRegistry.JEWELRY_COMPONENT);
            if (data != null) {
                for (BonusSource source : data.pattern().bonuses()) {
                    var bonuses = data.parts().stream().filter(part -> part.partId().equals(source.partIdForBonus())).findFirst().map(instance -> MaterialDataHandler.INSTANCE.get(instance.materialId()).bonuses()).orElse(List.of());
                    for (IBonus bonus : bonuses) {
                        if(bonus instanceof DeathBonus){
                            player.die(player.level().damageSources().fellOutOfWorld());
                        }
                    }
                }
            }
        }
    }
}

package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.IronsJewelry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ClientData.clear();
    }
}

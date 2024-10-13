package io.redspace.ironsjewelry.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsjewelry.IronsJewelry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ClientData.clear();
    }

    @SubscribeEvent
    public static void debug(InputEvent.Key event){
        if(event.getKey() == InputConstants.KEY_NUMPAD9){
            IronsJewelry.LOGGER.debug("activating debug!");
        }
    }
}

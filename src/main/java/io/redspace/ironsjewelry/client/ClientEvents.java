package io.redspace.ironsjewelry.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsjewelry.api.internal.AtlasHandler;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    private static boolean isShiftKeyDown;

    public static boolean isIsShiftKeyDown() {
        return isShiftKeyDown;
    }

    @SubscribeEvent
    public static void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        AtlasHandler.clear();
    }

    /**
     * Because we are storing the client data on the player itself instead of a static cache, we need to manually update it when the player reference changes
     */
    @SubscribeEvent
    public static void onClientClone(ClientPlayerNetworkEvent.Clone event) {
        if (event.getOldPlayer().getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            Minecraft.getInstance().player.setData(DataAttachmentRegistry.PLAYER_DATA, event.getOldPlayer().getData(DataAttachmentRegistry.PLAYER_DATA));
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        var button = event.getKey();
        var action = event.getAction();
        if (button == InputConstants.KEY_LSHIFT) {
            isShiftKeyDown = action >= InputConstants.PRESS;
        }
    }
}

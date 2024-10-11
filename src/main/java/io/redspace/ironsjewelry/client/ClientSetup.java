package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = IronsJewelry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.JEWELCRAFTING_MENU.get(), JewelcraftingStationScreen::new);
    }
}

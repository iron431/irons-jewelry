package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.compat.CompatHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus= EventBusSubscriber.Bus.MOD)
public class ModSetupEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CompatHandler.init();
        });
    }
}

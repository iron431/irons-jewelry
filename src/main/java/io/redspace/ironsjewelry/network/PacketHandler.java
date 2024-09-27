package io.redspace.ironsjewelry.network;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.network.packets.SetJewelcraftingStationPattern;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = IronsJewelry.MODID)
public class PacketHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(IronsJewelry.MODID).versioned("1.0.0").optional();

        payloadRegistrar.playToServer(SetJewelcraftingStationPattern.TYPE, SetJewelcraftingStationPattern.STREAM_CODEC, SetJewelcraftingStationPattern::handle);
        payloadRegistrar.playToClient(SyncJewelcraftingSlotStates.TYPE, SyncJewelcraftingSlotStates.STREAM_CODEC, SyncJewelcraftingSlotStates::handle);

    }
}

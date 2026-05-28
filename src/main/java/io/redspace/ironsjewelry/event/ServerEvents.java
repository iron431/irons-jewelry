package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerDataPacket(serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA)));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).tickCooldowns(1);
        }
    }
}

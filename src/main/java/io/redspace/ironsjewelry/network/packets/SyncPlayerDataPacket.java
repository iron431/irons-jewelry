package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientData;
import io.redspace.ironsjewelry.core.data.PlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerDataPacket(PlayerData playerData) implements CustomPacketPayload {

    public static final Type<SyncPlayerDataPacket> TYPE = new Type<>(IronsJewelry.id("clientbound_sync_player_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerDataPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncPlayerDataPacket::write, SyncPlayerDataPacket::new);

    public SyncPlayerDataPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(PlayerData.Serializer.networkRead(registryFriendlyByteBuf));
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        PlayerData.Serializer.networkWrite(registryFriendlyByteBuf, this.playerData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientData.localPlayerData = packet.playerData();
        });
    }
}

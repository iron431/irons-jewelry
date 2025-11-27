package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetBookmarkPacket(int bookmark) implements CustomPacketPayload {

    public static final Type<ServerboundSetBookmarkPacket> TYPE = new Type<>(IronsJewelry.id("serverbound_set_bookmark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetBookmarkPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundSetBookmarkPacket::write, ServerboundSetBookmarkPacket::new);

    public ServerboundSetBookmarkPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(registryFriendlyByteBuf.readInt());
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeInt(bookmark);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundSetBookmarkPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> PlayerData.get(context.player()).setBookmarkIndex(packet.bookmark()));
    }
}

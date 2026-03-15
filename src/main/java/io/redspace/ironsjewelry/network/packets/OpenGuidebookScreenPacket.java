package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenGuidebookScreenPacket implements CustomPacketPayload {

    public static final Type<OpenGuidebookScreenPacket> TYPE = new Type<>(IronsJewelry.id("clientbound_open_guidebook_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuidebookScreenPacket> STREAM_CODEC = CustomPacketPayload.codec(OpenGuidebookScreenPacket::write, OpenGuidebookScreenPacket::new);

    public OpenGuidebookScreenPacket(RegistryFriendlyByteBuf buf) {
    }

    public OpenGuidebookScreenPacket(){}

    private void write(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenGuidebookScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(ClientHelper::openGuidebookScreen);
    }
}

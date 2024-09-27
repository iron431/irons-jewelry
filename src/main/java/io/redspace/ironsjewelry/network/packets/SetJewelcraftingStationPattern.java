package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station.JewelcraftingStationMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetJewelcraftingStationPattern(int containerId,
                                             PatternDefinition patternDefinition) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetJewelcraftingStationPattern> TYPE = new CustomPacketPayload.Type<>(IronsJewelry.id("serverbound_set_jewelcrafting_station_pattern"));

    public static final StreamCodec<FriendlyByteBuf, SetJewelcraftingStationPattern> STREAM_CODEC = StreamCodec.of((buf, data) -> {
                buf.writeInt(data.containerId);
                buf.writeResourceLocation(data.patternDefinition.id());
            },
            (buf) -> new SetJewelcraftingStationPattern(buf.readInt(),PatternDataHandler.get(buf.readResourceLocation())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetJewelcraftingStationPattern packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu.containerId == packet.containerId()) {
                boolean flag = player.containerMenu instanceof JewelcraftingStationMenu jewelcraftingMenu && jewelcraftingMenu.handleSetPattern(packet.patternDefinition);
                if (flag) {
                    player.containerMenu.broadcastChanges();
                }
            }
        });
    }
}

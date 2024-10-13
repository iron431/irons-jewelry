package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationMenu;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public record SetJewelcraftingStationPattern(int containerId,
                                             Holder<PatternDefinition> patternDefinition) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetJewelcraftingStationPattern> TYPE = new CustomPacketPayload.Type<>(IronsJewelry.id("serverbound_set_jewelcrafting_station_pattern"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetJewelcraftingStationPattern> STREAM_CODEC = StreamCodec.of((buf, data) -> {
                buf.writeInt(data.containerId);
                buf.writeResourceKey(Objects.requireNonNull(data.patternDefinition.getKey()));
            },
            (buf) -> new SetJewelcraftingStationPattern(buf.readInt(),
                    IronsJewelryRegistries.patternRegistry(buf.registryAccess()).getHolderOrThrow(buf.readResourceKey(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY))));

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

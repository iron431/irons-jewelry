//package io.redspace.ironsjewelry.network.packets;
//
//import com.google.common.collect.Multimap;
//import io.redspace.ironsjewelry.IronsJewelry;
//import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
//import io.redspace.ironsjewelry.core.MaterialModifierDataHandler;
//import io.redspace.ironsjewelry.core.data.MaterialDefinition;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.Holder;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//import org.checkerframework.checker.units.qual.K;
//
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.Objects;
//
//public record SyncMaterialModifiersPacket(
//        Multimap<Holder<MaterialDefinition>, MaterialModifierDataHandler.Modifier> map) implements CustomPacketPayload {
//    public static final Type<SyncMaterialModifiersPacket> TYPE = new Type<>(IronsJewelry.id("clientbound_sync_material_modifiers"));
//
//    public static final StreamCodec<FriendlyByteBuf, SyncMaterialModifiersPacket> STREAM_CODEC = StreamCodec.of((buf, data) -> {
//                var entryList = data.map.entries().stream().toList();
//                int entries = entryList.size();
//                buf.writeInt(entries);
//                for (int i = 0; i < entries; i++) {
//                    var entry = entryList.get(i);
//                    buf.writeIdentifier(Objects.requireNonNull(entry.getKey().getKey()).location());
//                    var modifiers = data.map.get(entry.getKey());
//                    int modifierCount = modifiers.size();
//                    buf.writeInt(modifierCount);
//                    for (int j = 0; j < modifierCount; j++) {
//                        buf.rea
//                    }
//                }
//            },
//            (buf) -> {
//                var states = new ArrayList<SyncJewelcraftingSlotStates.SlotState>();
//                int i = buf.readInt();
//                for (int j = 0; j < i; j++) {
//                    states.add(new SyncJewelcraftingSlotStates.SlotState(buf.readInt(), buf.readInt(), buf.readBoolean()));
//                }
//                return new SyncMaterialModifiersPacket(states);
//            });
//
//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//
//    public static void handle(SyncJewelcraftingSlotStates packet, IPayloadContext context) {
//        context.enqueueWork(() -> {
//            if (Minecraft.getInstance().screen instanceof JewelcraftingStationScreen jewelScreen) {
//                jewelScreen.handleSlotSync(packet);
//            }
//        });
//    }
//}
package io.redspace.ironsjewelry.network.packets;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station.JewelcraftingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncJewelcraftingSlotStates(List<SlotState> slotStates) implements CustomPacketPayload {
    public record SlotState(int x, int y, boolean enabled) {
    }

    public static final Type<SyncJewelcraftingSlotStates> TYPE = new Type<>(IronsJewelry.id("clientbound_sync_jewelcrafting_slot_states"));

    public static final StreamCodec<FriendlyByteBuf, SyncJewelcraftingSlotStates> STREAM_CODEC = StreamCodec.of((buf, data) -> {
                var slotStates = data.slotStates;
                int i = slotStates.size();
                buf.writeInt(i);
                for (int j = 0; j < i; j++) {
                    buf.writeInt(slotStates.get(j).x);
                    buf.writeInt(slotStates.get(j).y);
                    buf.writeBoolean(slotStates.get(j).enabled);
                }
            },
            (buf) -> {
                var states = new ArrayList<SlotState>();
                int i = buf.readInt();
                for (int j = 0; j < i; j++) {
                    states.add(new SlotState(buf.readInt(), buf.readInt(), buf.readBoolean()));
                }
                return new SyncJewelcraftingSlotStates(states);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncJewelcraftingSlotStates packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().screen instanceof JewelcraftingStationScreen jewelScreen){
                jewelScreen.handleSlotSync(packet);
            }
        });
    }
}

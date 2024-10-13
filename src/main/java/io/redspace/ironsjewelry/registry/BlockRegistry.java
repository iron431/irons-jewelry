package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static final DeferredHolder<Block, JewelcraftingStationBlock> JEWELCRAFTING_STATION_BLOCK = BLOCKS.register("jewelcrafting_station", () -> new JewelcraftingStationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE)));
}

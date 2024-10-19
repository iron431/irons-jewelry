package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.registry.VillagerRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;

public class Setup {
    public static void commonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(()->{
            VillagerTrades.TRADES.put(VillagerRegistry.JEWELER_PROFESSSION.get(), new Int2ObjectOpenHashMap<>(Map.of(
                    1,
                    new VillagerTrades.ItemListing[]{
                            //new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_basic_pattern")), 2, 4, 1.5f, Trades::calculatePatternPrice),
                            //new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_advanced_pattern")), 2, 4, 1.5f, Trades::calculatePatternPrice),
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 8, 1.5f, Trades::calculateJewelryPrice),
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 8, 1.5f, Trades::calculateJewelryPrice)
                    },
                    2,
                    new VillagerTrades.ItemListing[]{
                    },
                    3,
                    new VillagerTrades.ItemListing[]{
                    },
                    4,
                    new VillagerTrades.ItemListing[]{

                    },
                    5,
                    new VillagerTrades.ItemListing[]{

                    }
            )));
        });
    }
}

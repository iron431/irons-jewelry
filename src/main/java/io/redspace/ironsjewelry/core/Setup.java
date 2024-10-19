package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.registry.VillagerRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;

public class Setup {
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            VillagerTrades.TRADES.put(VillagerRegistry.JEWELER_PROFESSSION.get(), new Int2ObjectOpenHashMap<>(Map.of(
                    1,
                    new VillagerTrades.ItemListing[]{
                            new Trades.BuyItem(Items.GOLD_INGOT, 6, 1, 12, 1, 0.05F),
                            new Trades.BuyItem(Items.COPPER_INGOT, 9, 1, 12, 1, 0.05F),

                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_default_jewelry")), 2, 8, 0.5f, Trades::calculateJewelryPrice),
                            //new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 8, 1.5f, Trades::calculateJewelryPrice)
                    },
                    2,
                    new VillagerTrades.ItemListing[]{
                            new Trades.BuyItem(Items.DIAMOND, 1, 1, 12, 10, 0.05F),
                            new Trades.BuyItem(Items.AMETHYST_SHARD, 2, 1, 12, 10, 0.05F),
                            new Trades.BuyItem(Items.LAPIS_LAZULI, 6, 2, 12, 8, 0.05F),
                            //TODO: other gems
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice)

                    },
                    3,
                    new VillagerTrades.ItemListing[]{
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_basic_pattern")), 1, 25, 0.5f, Trades::calculatePatternPrice),
                    },
                    4,
                    new VillagerTrades.ItemListing[]{
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice),
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice),
                    },
                    5,
                    new VillagerTrades.ItemListing[]{
                            new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_advanced_pattern")), 1, 25, 0.5f, Trades::calculatePatternPrice),
                    }
            )));
        });
    }
}

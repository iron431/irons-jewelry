package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Trades;
import io.redspace.ironsjewelry.registry.VillagerRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber
public class SetupEvents {
    @SubscribeEvent
    public static void setupProfessionTrades(VillagerTradesEvent event) {
        if (event.getType().equals(VillagerRegistry.JEWELER_PROFESSSION.get())) {
            var trades = event.getTrades();
            var novice = trades.get(1);
            var apprentice = trades.get(2);
            var journeyman = trades.get(3);
            var expert = trades.get(4);
            var master = trades.get(5);
            /*
            Novice
             */
            novice.add(new Trades.BuyItem(Items.GOLD_INGOT, 6, 1, 12, 1, 0.05F));
            novice.add(new Trades.BuyItem(Items.COPPER_INGOT, 9, 1, 12, 1, 0.05F));
            novice.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_default_jewelry")), 2, 8, 0.5f, Trades::calculateJewelryPrice));
            /*
            Apprentice
             */
            apprentice.add(new Trades.BuyItem(Items.DIAMOND, 1, 1, 12, 10, 0.05F));
            apprentice.add(new Trades.BuyItem(Items.AMETHYST_SHARD, 2, 1, 12, 10, 0.05F));
            apprentice.add(new Trades.BuyItem(Items.LAPIS_LAZULI, 6, 2, 12, 8, 0.05F));
            apprentice.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice));
            /*
            Journeyman
             */
            journeyman.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_basic_pattern")), 1, 25, 0.5f, Trades::calculatePatternPrice));
            /*
            Expert
             */
            expert.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice));
            expert.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 2, 15, 0.5f, Trades::calculateJewelryPrice));
            /*
            Master
             */
            master.add(new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_advanced_pattern")), 1, 25, 0.5f, Trades::calculatePatternPrice));

        }
    }

    @SubscribeEvent
    public static void addWanderingTrades(WandererTradesEvent event) {
        //doing this twice increases weight and allows multiple trades per instance (not rare though, because there's always only one)
        for (int i = 0; i < 2; i++) {
            event.getGenericTrades().add(
                    new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/sell_jewelry")), 4, 25, 0.5f, Trades::calculateJewelryPrice));
            event.getRareTrades().add(
                    new Trades.SellLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("trades/wandering_trader_sell_pattern")), 2, 25, 0.5f, Trades::calculatePatternPrice));
        }
    }
}

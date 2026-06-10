package io.redspace.ironsjewelry.event;

import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class SetupEvents {
    // TODO: Villager trades are now fully data-driven in MC 26.1.2.
    //  VillagerTradesEvent and WandererTradesEvent have been removed.
    //  Migrate jeweler profession trades and wandering trader trades to JSON datapack format
    //  using the new VillagerTrade registry (net.minecraft.world.item.trading.VillagerTrade).
    //  See data/minecraft/villager_trade/ for vanilla examples.
    //  The Trades utility class (io.redspace.ironsjewelry.utils.Trades) also needs migration
    //  since VillagerTrades.ItemListing no longer exists.
}

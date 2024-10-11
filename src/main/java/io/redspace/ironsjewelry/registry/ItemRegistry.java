package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.gameplay.item.CurioBaseItem;
import io.redspace.ironsjewelry.gameplay.item.PatternRecipeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, CurioBaseItem> RING = ITEMS.register("ring", () -> new CurioBaseItem(new Item.Properties().stacksTo(1), "ring"));
    public static final DeferredHolder<Item, PatternRecipeItem> RECIPE = ITEMS.register("recipe", () -> new PatternRecipeItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, BlockItem> JEWELCRAFTING_STATION_BLOCK_ITEM = ITEMS.register("jewelcrafting_station", () -> new BlockItem(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get(), new Item.Properties()));
}

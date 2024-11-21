package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.datagen.ItemModelDataGenerator;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.item.PatternRecipeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, CurioBaseItem> RING = ITEMS.register("ring", () -> new CurioBaseItem(new Item.Properties().stacksTo(1), "ring"));
    public static final DeferredHolder<Item, CurioBaseItem> NECKLACE = ITEMS.register("necklace", () -> new CurioBaseItem(new Item.Properties().stacksTo(1), "necklace"));
    public static final DeferredHolder<Item, PatternRecipeItem> RECIPE = ITEMS.register("recipe", () -> new PatternRecipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> RUBY = registerSimpleItem("ruby", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAPPHIRE = registerSimpleItem("sapphire", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TOPAZ = registerSimpleItem("topaz", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOONSTONE = registerSimpleItem("moonstone", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PERIDOT = registerSimpleItem("peridot", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ONYX = registerSimpleItem("onyx", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GARNET = registerSimpleItem("garnet", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> JEWELCRAFTING_STATION_BLOCK_ITEM = ITEMS.register("jewelcrafting_station", () -> new BlockItem(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get(), new Item.Properties()));

    private static <T extends Item> DeferredHolder<Item, T> registerSimpleItem(String name, Supplier<T> supplier) {
        var s = ITEMS.register(name, supplier);
        ItemModelDataGenerator.toRegister.add(generator -> generator.simpleItem(s));
        return s;
    }
}

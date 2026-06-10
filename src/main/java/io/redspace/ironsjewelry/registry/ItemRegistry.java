package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.datagen.ItemModelDataGenerator;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.item.PatternRecipeItem;
import io.redspace.ironsjewelry.item.book.GuideBookItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.function.Function;

public class ItemRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, CurioBaseItem> RING = ITEMS.registerItem("ring", (properties) -> new CurioBaseItem(properties.stacksTo(1), "ring"));
    public static final DeferredHolder<Item, CurioBaseItem> NECKLACE = ITEMS.registerItem("necklace", (properties) -> new CurioBaseItem(properties.stacksTo(1), "necklace"));
    public static final DeferredHolder<Item, PatternRecipeItem> RECIPE = registerSimpleItem("recipe", (properties) -> new PatternRecipeItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, GuideBookItem> JEWELCRAFTING_GUIDE = registerSimpleItem("jewelcrafting_guide", (properties) -> new GuideBookItem(properties.rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> RUBY = registerSimpleItem("ruby", Item::new);
    public static final DeferredHolder<Item, Item> SAPPHIRE = registerSimpleItem("sapphire", Item::new);
    public static final DeferredHolder<Item, Item> TOPAZ = registerSimpleItem("topaz", Item::new);
    public static final DeferredHolder<Item, Item> MOONSTONE = registerSimpleItem("moonstone", Item::new);
    public static final DeferredHolder<Item, Item> PERIDOT = registerSimpleItem("peridot", Item::new);
    public static final DeferredHolder<Item, Item> ONYX = registerSimpleItem("onyx", Item::new);
    public static final DeferredHolder<Item, Item> GARNET = registerSimpleItem("garnet", Item::new);

    public static final DeferredHolder<Item, BlockItem> JEWELCRAFTING_STATION_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("jewelcrafting_station", BlockRegistry.JEWELCRAFTING_STATION_BLOCK);

    private static <T extends Item> DeferredHolder<Item, T> registerSimpleItem(String name, Function<Item.Properties, T> supplier) {
        var s = ITEMS.registerItem(name, supplier);
        ItemModelDataGenerator.simpleItem(s);
        return s;
    }

    public static Collection<DeferredHolder<Item, ? extends Item>> items() {
        return ITEMS.getEntries();
    }
}

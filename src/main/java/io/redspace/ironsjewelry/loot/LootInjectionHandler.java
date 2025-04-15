package io.redspace.ironsjewelry.loot;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.JewelryModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@EventBusSubscriber
public class LootInjectionHandler {
    /**
     * If a loot table has greater than or equal to this value gearscore, jewelry is allowed to generate into it
     */
    private static final int GEARSCORE_THRESHOLD = 30;
    private static final Map<Predicate<Item>, Integer> ITEM_GEARSCORES = createGearscoreMap();
    public static final HashMap<ResourceLocation, Float> TRACKED_LOOT_TABLES = new HashMap<>();

    //fixme: item tags arent loaded yet
    // solution - why use loot table event? just use on data finished loading and manually iterate over loot tables after all data n tags are loaded
    private static Map<Predicate<Item>, Integer> createGearscoreMap() {
        Map<Predicate<Item>, Integer> map = new HashMap<>();
        map.put(is(JewelryModTags.LOOT_HANDLER_LOW_GEARSCORE), 5);
        map.put(is(JewelryModTags.LOOT_HANDLER_MEDIUM_GEARSCORE), 15);
        map.put(is(JewelryModTags.LOOT_HANDLER_HIGH_GEARSCORE), 30);
        map.put(is(JewelryModTags.LOOT_HANDLER_VERY_HIGH_GEARSCORE), 75);
        map.put(item -> item instanceof ArmorItem || item instanceof SwordItem, 25);
        return map;
    }

    private static Predicate<Item> is(TagKey<Item> tag) {
        return item -> item.getDefaultInstance().is(tag);
//        return item -> BuiltInRegistries.ITEM.getTag(tag).stream().anyMatch(set -> set.contains(BuiltInRegistries.ITEM.wrapAsHolder(item)));
    }

    private static int gearscoreFor(Item item) {
        for (Map.Entry<Predicate<Item>, Integer> gearscore : ITEM_GEARSCORES.entrySet()) {
            if (gearscore.getKey().test(item)) {
                return gearscore.getValue();
            }
        }
        return 0;
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
//        if (LOOT_POOL_FACTORY == null) {
//            IronsJewelry.LOGGER.error("Could not inject loot into {}, factory not initialized!", event.getName());
//            return;
//        }
        var id = event.getName();
        if (id.getPath().startsWith("blocks") || id.getPath().startsWith("entities") || id.getPath().startsWith("equipment")) {
            // skip tons of trivial tables
            return;
        }
        int gearscore = 0;
        var table = event.getTable();
        if (table.pools.size() == 1 && table.pools.getFirst().getRolls() instanceof ConstantValue cv && cv.value() == 1) {
            // some special loot tables, like bartering, pot drops, or archeology, can only give 1 item at a time and cant support injections in this style
            return;
        }
        for (LootPool pool : table.pools) {
            for (LootPoolEntryContainer entry : pool.entries) {
                if (entry instanceof LootItem itemHolder) {
                    var item = itemHolder.item;
                    if (item instanceof CurioBaseItem) {
                        // loot table already handles jewelry, skip it
                        IronsJewelry.LOGGER.debug("LootTable {} already contains jewelry, skipping", id);
                        return;
                    }
                    gearscore += gearscoreFor(item.value());
                    //todo: remove debuggers
                    if (gearscoreFor(item.value()) > 0) {
                        IronsJewelry.LOGGER.debug("LootTable {} item {} score {}", id, item.value(), gearscoreFor(item.value()));
                    }
                }
            }
        }
        if (gearscore >= GEARSCORE_THRESHOLD) {
            float chance = Mth.clampedLerp(0.05f, 0.5f, (gearscore - GEARSCORE_THRESHOLD) / 1000f);
//            table.pools.add(LOOT_POOL_FACTORY.apply(chance));
            TRACKED_LOOT_TABLES.put(id, chance);
        }
    }


//    private static Function<Float, LootPool> LOOT_POOL_FACTORY = null;
//
//    @SubscribeEvent
//    public static void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
//        var patternRegistry = IronsJewelryRegistries.patternRegistry(event.getServer().registryAccess());
//        var generic = patternRegistry.getOrCreateTag(JewelryModTags.GENERIC_LOOTABLE);
//        var highQuality = patternRegistry.getOrCreateTag(JewelryModTags.HIGH_QUALITY_LOOTABLE);
//        LOOT_POOL_FACTORY = chance -> new LootPool.Builder()
//                .setRolls(new ConstantValue(1))
//                .when(LootItemRandomChanceCondition.randomChance(chance))
//                .add(LootItem.lootTableItem(ItemRegistry.RING.get()).setWeight(6)
//                        .apply(new GenerateJewelryLootFunction.Builder().withPatterns(generic)))
//                .add(LootItem.lootTableItem(ItemRegistry.NECKLACE.get()).setWeight(2)
//                        .apply(new GenerateJewelryLootFunction.Builder().withPatterns(generic)))
//                .add(LootItem.lootTableItem(ItemRegistry.RING.get()).setWeight(3)
//                        .apply(new GenerateJewelryLootFunction.Builder().withPatterns(highQuality)))
//                .add(LootItem.lootTableItem(ItemRegistry.NECKLACE.get()).setWeight(1)
//                        .apply(new GenerateJewelryLootFunction.Builder().withPatterns(highQuality)))
//                .build();
//    }

}

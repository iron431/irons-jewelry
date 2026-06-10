package io.redspace.ironsjewelry.loot;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.utils.JewelryModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@EventBusSubscriber
public class LootInjectionHandler {
    /**
     * If a loot table has greater than or equal to this value gearscore, jewelry is allowed to generate into it
     */
    private static final int GEARSCORE_THRESHOLD = 30;
    private static final Map<Predicate<Item>, Integer> ITEM_GEARSCORES = createGearscoreMap();
    public static final HashMap<Identifier, Float> TRACKED_LOOT_TABLES = new HashMap<>();
    private static boolean built;

    private static Map<Predicate<Item>, Integer> createGearscoreMap() {
        Map<Predicate<Item>, Integer> map = new HashMap<>();
        map.put(is(JewelryModTags.LOOT_HANDLER_LOW_GEARSCORE), 5);
        map.put(is(JewelryModTags.LOOT_HANDLER_MEDIUM_GEARSCORE), 25);
        map.put(is(JewelryModTags.LOOT_HANDLER_HIGH_GEARSCORE), 50);
        map.put(is(JewelryModTags.LOOT_HANDLER_VERY_HIGH_GEARSCORE), 75);
        map.put(item -> item.components().has(net.minecraft.core.component.DataComponents.EQUIPPABLE) || item.components().has(net.minecraft.core.component.DataComponents.TOOL), 25);
        return map;
    }

    private static Predicate<Item> is(TagKey<Item> tag) {
        return item -> item.builtInRegistryHolder().is(tag);
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
    public static void cacheTrackedLootTables(OnDatapackSyncEvent event) {
        // if never built, or the world data is reloading (player is null) then do work
        if (!built || event.getPlayer() == null) {
//            var lootTables = event.getPlayerList().getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE);
            var lootTables = event.getPlayerList().getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElements().toList();
            TRACKED_LOOT_TABLES.clear();
            for (var registryEntry : lootTables) {
                handleLootTable(registryEntry.getKey().identifier(), registryEntry.value());
            }
            built = true;
        }
    }

    private static void handleLootTable(Identifier id, LootTable table) {
        try {
            // assume our loot tables do not need any edits
            if (id.getNamespace().equals(IronsJewelry.MODID)) {
                return;
            }
            // skip tons of trivial tables
            if (!(id.getPath().startsWith("chests") || id.getPath().startsWith("spawners"))) {
                return;
            }
            // some special loot tables, like bartering, pot drops, or archeology, can only give 1 item at a time, so do not append more rolls
            if (table.pools.size() == 1 && table.pools.getFirst().getRolls() instanceof ConstantValue cv && cv.value() == 1) {
                return;
            }
            int gearscore = 0;
            for (LootPool pool : table.pools) {
                for (LootPoolEntryContainer entry : pool.entries) {
                    if (entry instanceof LootItem itemHolder) {
                        var item = itemHolder.item;
                        if (item.value() instanceof CurioBaseItem) {
                            // loot table already handles jewelry, skip it
                            return;
                        }
                        gearscore += gearscoreFor(item.value());
                    }
                }
            }
            if (gearscore >= GEARSCORE_THRESHOLD) {
                float chance = Mth.clampedLerp(0.025f, 0.5f, (gearscore - GEARSCORE_THRESHOLD) / 750f);
                TRACKED_LOOT_TABLES.put(id, chance);
            }
        } catch (Exception e) {
            IronsJewelry.LOGGER.error("Failed to parse loot table \"{}\", skipping: {}", id, e.getMessage());
        }
    }
}

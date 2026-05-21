package io.redspace.ironsjewelry.utils;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class JewelryModTags {
    public static final TagKey<Item> LOOT_HANDLER_LOW_GEARSCORE
            = TagKey.create(Registries.ITEM, IronsJewelry.id("loot_handler/low_gearscore"));
    public static final TagKey<Item> LOOT_HANDLER_MEDIUM_GEARSCORE
            = TagKey.create(Registries.ITEM, IronsJewelry.id("loot_handler/medium_gearscore"));
    public static final TagKey<Item> LOOT_HANDLER_HIGH_GEARSCORE
            = TagKey.create(Registries.ITEM, IronsJewelry.id("loot_handler/high_gearscore"));
    public static final TagKey<Item> LOOT_HANDLER_VERY_HIGH_GEARSCORE
            = TagKey.create(Registries.ITEM, IronsJewelry.id("loot_handler/very_high_gearscore"));

    public static final TagKey<MaterialDefinition> JEWELRY_LOOT_MATERIAL_BLACKLIST
            = TagKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("jewelry_loot_blacklist"));
    public static final TagKey<MaterialDefinition> METAL
            = TagKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("metal"));
    public static final TagKey<MaterialDefinition> GEM
            = TagKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("gem"));
    public static final TagKey<MaterialDefinition> GOLD
            = TagKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("gold"));
    public static final TagKey<MaterialDefinition> EMERALD
            = TagKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("emerald"));

    public static final TagKey<PatternDefinition> GENERIC_LOOTABLE
            = TagKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, IronsJewelry.id("generic_lootable"));
    public static final TagKey<PatternDefinition> HIGH_QUALITY_LOOTABLE
            = TagKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, IronsJewelry.id("high_quality_lootable"));
}

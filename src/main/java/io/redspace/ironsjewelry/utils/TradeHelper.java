package io.redspace.ironsjewelry.utils;

import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.StoredPatternData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Optional;
import java.util.function.BiFunction;

public class TradeHelper {

    public static int getSpecialItemPrice(ItemStack stack) {
        if (JewelryData.has(stack)) {
            return calculateJewelryPrice(stack);
        } else if (StoredPatternData.has(stack)) {
            return calculatePatternPrice(stack);
        }
        return 0;
    }

    public static int calculatePatternPrice(ItemStack stack) {
        return calculatePatternPrice(StoredPatternData.get(stack));
    }

    public static int calculatePatternPrice(Holder<PatternDefinition> heldPattern) {
        if (heldPattern != null) {
            var pattern = heldPattern.value();
            return (int) ((17 + pattern.partTemplate().size() * 2) * pattern.qualityMultiplier());
        }
        return 0;
    }

    public static int calculateJewelryPrice(JewelryData jewelryData) {
        int cost = (int) (12 * jewelryData.pattern().value().qualityMultiplier());
        for (Holder<MaterialDefinition> part : jewelryData.parts().values()) {
            cost += (int) (4 * part.value().quality());
        }
        if (jewelryData.pattern().value().partForQuality().isPresent()) {
            cost = (int) (cost * jewelryData.parts().get(jewelryData.pattern().value().partForQuality().get()).value().quality());
        }
        return cost;
    }

    public static int calculateJewelryPrice(ItemStack stack) {
        var jewelryData = JewelryData.getNullable(stack);
        if (jewelryData != null && jewelryData.isValid()) {
            return calculateJewelryPrice(jewelryData);
        }
        return 0;
    }

}

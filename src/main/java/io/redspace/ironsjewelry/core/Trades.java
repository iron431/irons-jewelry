package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Optional;
import java.util.function.BiFunction;

public class Trades {

    public static int calculatePatternPrice(ItemStack stack, RandomSource randomSource) {
        var heldPattern = stack.get(ComponentRegistry.STORED_PATTERN);
        if (heldPattern != null) {
            var pattern = heldPattern.value();
            return (int) ((randomSource.nextIntBetweenInclusive(15, 20) + pattern.partTemplate().size() * 2) * pattern.qualityMultiplier());
        }
        return 0;
    }

    public static int calculateJewelryPrice(ItemStack stack, RandomSource randomSource) {
        var jewelryData = stack.get(ComponentRegistry.JEWELRY_COMPONENT);
        if (jewelryData != null && jewelryData.isValid()) {
            int cost = (int) (12 * jewelryData.pattern().value().qualityMultiplier());
            for (Holder<MaterialDefinition> part : jewelryData.parts().values()) {
                cost += (int) (4 * part.value().quality());
            }
            if (jewelryData.pattern().value().partForQuality().isPresent()) {
                cost = (int) (cost * jewelryData.parts().get(jewelryData.pattern().value().partForQuality().get()).value().quality());
            }
            return cost;
        }
        return 0;
    }

    public static class SellItem implements VillagerTrades.ItemListing {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;
        private final Optional<ResourceKey<EnchantmentProvider>> enchantmentProvider;

        public SellItem(Block pBlock, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(new ItemStack(pBlock), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
        }

        public SellItem(Item pItem, int pEmeraldCost, int pNumberOfItems, int pVillagerXp) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, 12, pVillagerXp);
        }

        public SellItem(Item pItem, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
        }

        public SellItem(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(pItemStack, pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, 0.05F);
        }

        public SellItem(Item pItem, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp, float pPriceMultiplier) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, pPriceMultiplier);
        }

        public SellItem(
                Item pItem, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp, float pPriceMultiplier, ResourceKey<EnchantmentProvider> pEnchantmentProvider
        ) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, pPriceMultiplier, Optional.of(pEnchantmentProvider));
        }

        public SellItem(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp, float pPriceMultiplier) {
            this(pItemStack, pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, pPriceMultiplier, Optional.empty());
        }

        public SellItem(
                ItemStack pItemStack,
                int pEmeraldCost,
                int pNumberOfItems,
                int pMaxUses,
                int pVillagerXp,
                float pPriceMultiplier,
                Optional<ResourceKey<EnchantmentProvider>> pEnchantmentProvider
        ) {
            this.itemStack = pItemStack;
            this.emeraldCost = pEmeraldCost;
            this.itemStack.setCount(pNumberOfItems);
            this.maxUses = pMaxUses;
            this.villagerXp = pVillagerXp;
            this.priceMultiplier = pPriceMultiplier;
            this.enchantmentProvider = pEnchantmentProvider;
        }

        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            ItemStack itemstack = this.itemStack.copy();
            Level level = pTrader.level();
            this.enchantmentProvider
                    .ifPresent(
                            p_348340_ -> EnchantmentHelper.enchantItemFromProvider(
                                    itemstack,
                                    level.registryAccess(),
                                    (ResourceKey<EnchantmentProvider>) p_348340_,
                                    level.getCurrentDifficultyAt(pTrader.blockPosition()),
                                    pRandom
                            )
                    );
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    public record BuyItem(Item toBuy, int toBuyCount, int emeraldCost, int maxUses, int villagerXp,
                          float priceMultiplier) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            return new MerchantOffer(new ItemCost(toBuy, toBuyCount), new ItemStack(Items.EMERALD, this.emeraldCost), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    public record SellLootTable(ResourceKey<LootTable> lootTable, int maxUses, int villagerXp,
                                float priceMultiplier,
                                BiFunction<ItemStack, RandomSource, Integer> itemCostFunction) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            if (pTrader.level() instanceof ServerLevel serverLevel) {

                LootTable loottable = serverLevel.getServer().reloadableRegistries().getLootTable(this.lootTable);
                var context = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY);
                var items = loottable.getRandomItems(context);
                if (!items.isEmpty()) {
                    var stack = items.getFirst();
                    int price = itemCostFunction.apply(stack, pRandom);
                    ItemCost primaryCost;
                    Optional<ItemCost> secondaryCost = Optional.empty();
                    if (price > 64 * 9) {
                        price /= 9;
                        primaryCost = new ItemCost(Items.EMERALD_BLOCK, 64);
                        secondaryCost = Optional.of(new ItemCost(Items.EMERALD_BLOCK, price - 64));
                    } else if (price > 64) {
                        int blocks = price / 9;
                        primaryCost = new ItemCost(Items.EMERALD_BLOCK, blocks);
                        secondaryCost = Optional.of(new ItemCost(Items.EMERALD, price % 9));
                    } else {
                        primaryCost = new ItemCost(Items.EMERALD, price);
                    }
                    return new MerchantOffer(primaryCost, secondaryCost, stack, this.maxUses, this.villagerXp, this.priceMultiplier);
                }
            }
            return null;
        }
    }

    public record BuyItemTag(TagKey<Item> itemTag, int buyCount, int emeraldCost, int maxUses, int villagerXp,
                             float priceMultiplier) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            if (pTrader.level() instanceof ServerLevel serverLevel) {
                var options = BuiltInRegistries.ITEM.getTag(this.itemTag);
                if (options.isPresent()) {
                    var set = options.get();
                    var item = set.getRandomElement(pRandom);
                    if (item.isPresent()) {
                        return new MerchantOffer(new ItemCost(item.get().value(), buyCount), new ItemStack(Items.EMERALD, emeraldCost), this.maxUses, this.villagerXp, this.priceMultiplier);
                    }
                }
            }
            return null;
        }
    }

    public record SellItemTag(TagKey<Item> itemTag, int maxUses, int villagerXp,
                              float priceMultiplier,
                              int emeraldValue) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            if (pTrader.level() instanceof ServerLevel serverLevel) {
                var options = BuiltInRegistries.ITEM.getTag(this.itemTag);
                if (options.isPresent()) {
                    var set = options.get();
                    var item = set.getRandomElement(pRandom);
                    if (item.isPresent()) {
                        var stack = new ItemStack(item.get());
                        return new MerchantOffer(new ItemCost(Items.EMERALD, emeraldValue), stack, this.maxUses, this.villagerXp, this.priceMultiplier);
                    }
                }
            }
            return null;
        }
    }
}

package io.redspace.ironsjewelry.mixin;

import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.utils.Trades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerTrade.class)
public class VillagerTradeMixin {
    @Inject(method = "getOffer", at = @At("RETURN"), cancellable = true)
    private static void wrapCustomJewelryCost(LootContext lootContext, CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        if (offer == null) {
            return;
        }
        if (!offer.getCostA().is(Items.EMERALD)) {
            return;
        }
        JewelryData data = JewelryData.getNullable(offer.getResult());
        if (data == null || !data.isValid()) {
            return;
        }
        ItemCost primaryCost;
        Optional<ItemCost> secondaryCost = Optional.empty();
        int emeraldPrice = Trades.calculateJewelryPrice(data);
        if (emeraldPrice > 64 * 9) {
            // price is greater than one stack of blocks, therefore the inputs are in units of blocks
            emeraldPrice = emeraldPrice - 64 * 9;
            primaryCost = new ItemCost(Items.EMERALD_BLOCK, 64);
            if (emeraldPrice >= 9) {
                secondaryCost = Optional.of(new ItemCost(Items.EMERALD_BLOCK, Math.max(1, emeraldPrice / 9)));
            }
        } else if (emeraldPrice > 64) {
            // price is greater than one stack, therefore inputs are in blocks and change
            int blocks = emeraldPrice / 9;
            primaryCost = new ItemCost(Items.EMERALD_BLOCK, blocks);
            emeraldPrice = emeraldPrice - blocks * 9;
            if (emeraldPrice > 0) {
                secondaryCost = Optional.of(new ItemCost(Items.EMERALD, emeraldPrice % 9));
            }
        } else {
            // price is less than one stack, literal interpretation
            primaryCost = new ItemCost(Items.EMERALD, emeraldPrice);
        }
        // swap with copy of merchant offer using dynamic price
        cir.setReturnValue(new MerchantOffer(
                primaryCost,
                secondaryCost,
                offer.getResult(),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier()
        ));
    }
}

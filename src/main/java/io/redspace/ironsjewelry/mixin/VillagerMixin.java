package io.redspace.ironsjewelry.mixin;

import io.redspace.ironsjewelry.core.bonuses.TradeDiscountBonusType;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {

    @Inject(method = "updateSpecialPrices", at = @At(value = "RETURN"))
    void irons_jewelry$handleHagglerBonus(Player player, CallbackInfo ci) {
        Utils.getEquippedBonuses(player).stream().filter(bonus -> bonus.bonusType().equals(BonusTypeRegistry.TRADE_DISCOUNT_BONUS.get())).forEach(
                bonus -> {
                    for (MerchantOffer merchantoffer : ((Villager) (Object) this).getOffers()) {
                        merchantoffer.addToSpecialPriceDiff(-((TradeDiscountBonusType) bonus.bonusType()).getItemDiscount(bonus.quality()));
                    }
                }
        );
    }
}

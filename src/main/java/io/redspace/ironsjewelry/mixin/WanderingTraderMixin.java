package io.redspace.ironsjewelry.mixin;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Trades;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public class WanderingTraderMixin {
    @Inject(method = "updateTrades", at = @At(value = "RETURN"))
    private void injectGemTrades(CallbackInfo ci) {
        WanderingTrader wanderingTrader = (WanderingTrader) (Object) this;
        var trade = new Trades.SellItemTag(TagKey.create(Registries.ITEM, IronsJewelry.id("jeweler_sellable_gems")), 8, 15, 0.5f, 6);
        var offer = trade.getOffer(wanderingTrader, wanderingTrader.getRandom());
        var offers = wanderingTrader.getOffers();
        if (offer != null) {
            offers.add(wanderingTrader.getRandom().nextInt(offers.size() - 1), offer);
        }
    }
}

package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class TradeDiscountBonusType extends BonusType {
    @Override
    public IBonusParameterType<?> getParameterType() {
        return ParameterTypeRegistry.EMPTY.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        int itemDiscount = getItemDiscount(bonus.quality());
        String desc = getDescriptionId() + (itemDiscount == 1 ? ".description.singular" : ".description");
        return List.of(Component.literal(" ").append(Component.translatable(desc, itemDiscount).withStyle(ChatFormatting.YELLOW)));
    }

    public int getItemDiscount(double quality) {
        return 1 + Math.max(0, Mth.floor(quality - 1));
    }
}

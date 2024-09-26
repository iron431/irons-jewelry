package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public record BonusInstance(Bonus bonus, double quality, Map<IBonusParameterType<?>, Object> parameter) {
    public List<Component> getTooltipDescription() {
        return this.bonus.getTooltipDescription(this);
    }
}

package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record BonusInstance(BonusType bonusType, double quality, Map<IBonusParameterType<?>, Object> parameter, Optional<QualityScalar> cooldown) {
    public List<Component> getTooltipDescription() {
        return this.bonusType.getTooltipDescription(this);
    }
}

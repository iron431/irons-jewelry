package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class EmptyBonus extends Bonus {
    @Override
    public IBonusParameterType<?> getParameterType() {
        return ParameterTypeRegistry.EMPTY.get();
    }
}
